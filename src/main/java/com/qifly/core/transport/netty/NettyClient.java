package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.frame.FrameCodec;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.retry.RetryExecutor;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class NettyClient implements TransportClient {

    Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final Bootstrap bootstrap;
    private final EventLoopGroup workGroup = new NioEventLoopGroup(1);
    private volatile Channel channel;
    private final ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    private final int maxFrameLength = 1024 * 1024 + 16;
    private final AtomicLong requestId = new AtomicLong(1);
    private final ConcurrentMap<Long, CompletableFuture<Any>> futureMap = new ConcurrentHashMap<>();

    public NettyClient() {
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
    public void connect(String host, int port) throws InterruptedException {
        ChannelFuture f = bootstrap.connect(host, port).sync();
        if (!f.isSuccess()) {
            RetryExecutor.executeAsync("netty-connect", () -> {
                ChannelFuture future = bootstrap.connect(host, port).sync();
                if (!future.isSuccess()) {
                    throw new RuntimeException("connect failed");
                }
                notifyConnect(future, host, port);
            });
            return;
        }
        notifyConnect(f, host, port);
    }

    private void notifyConnect(ChannelFuture future, String host, int port) {
        channel = future.channel();
        channelMap.put(host + ":" + port, channel);
        logger.info("netty client connect {}:{} success", host, port);
    }

    @Override
    public void disconnect(String endpoint) throws InterruptedException {
        Channel ch = getChannel(endpoint);
        if (ch != null) {
            ChannelFuture f = ch.close().sync();
            if (!f.isSuccess()) {
                RetryExecutor.executeAsync("netty-connect", () -> {
                    ChannelFuture future = ch.close().sync();
                    if (!future.isSuccess()) {
                        throw new RuntimeException("disconnect failed");
                    }
                    notifyDisconnect(endpoint);
                });
                return;
            }
        }
        notifyDisconnect(endpoint);
    }

    private void notifyDisconnect(String endpoint) {
        channelMap.remove(endpoint);
        logger.info("netty client disconnect {} success", endpoint);
    }

    @Override
    public Channel getChannel(String endpoint) {
        if (channelMap.get(endpoint) == null) {
            return null;
        }
        return channelMap.get(endpoint);
    }

    @Override
    public CompletableFuture<Any> send(String endpoint, RpcBody body) {
        long id = requestId.getAndIncrement();
        CompletableFuture<Any> future = new CompletableFuture<>();
        futureMap.put(id, future);
        Channel ch = getChannel(endpoint);
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