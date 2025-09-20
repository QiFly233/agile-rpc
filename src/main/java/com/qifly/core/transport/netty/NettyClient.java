package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.frame.FrameCodec;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.service.Consumer;
import com.qifly.core.transport.TransportClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class NettyClient implements TransportClient {

    Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final List<Consumer> consumers;
    private final Bootstrap bootstrap;
    private final EventLoopGroup workGroup = new NioEventLoopGroup(1);
    private volatile Channel channel;
    private final ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    private final int maxFrameLength = 1024 * 1024 + 16;
    private final AtomicLong requestId = new AtomicLong(1);
    private final ConcurrentMap<Long, CompletableFuture<Any>> futureMap = new ConcurrentHashMap<>();

    public NettyClient(List<Consumer> consumers) {
        this.consumers = consumers;
        bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4, 8, 0));
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 180)); // 心跳机制
                        ch.pipeline().addLast(new FrameCodec());
                        ch.pipeline().addLast(new ClientHandler(futureMap));
                    }
                });
    }

    @Override
    public void connect(String host, int port) throws Exception {
        ChannelFuture f = bootstrap.connect(host, port).sync();
        if (!f.isSuccess()) {
            throw new IOException("connect failed", f.cause());
        }
        channel = f.channel();
        channelMap.put(host + ":" + port, channel);
        logger.info("netty client connect host{}:port {}", host, port);
    }

    @Override
    public Channel getChannel(String host, int port) {
        if (channelMap.get(host + ":" + port) == null) {
            return null;
        }
        return channelMap.get(host + ":" + port);
    }

    @Override
    public CompletableFuture<Any> send(int rpcId, Message req) {
        long id = requestId.getAndIncrement();
        CompletableFuture<Any> future = new CompletableFuture<>();
        futureMap.put(id, future);

        Channel ch = getChannel("127.0.0.1", 8080);
        RpcBody body = RpcBody.newBuilder()
                .setRpcId(rpcId)
                .setData(Any.pack(req)).build();
        ch.writeAndFlush(RpcFrame.request(1, false, id, Unpooled.wrappedBuffer(body.toByteArray())));
        return future;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        workGroup.shutdownGracefully();
    }
}