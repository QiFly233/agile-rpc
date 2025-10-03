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
    public void connect(String host, int port) {
        doConnect(host, port, 0);
    }

    private void doConnect(String host, int port, int retryCount) {
        bootstrap.connect(host, port).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                logger.error("netty client connect {}:{} failed, retryCount={}", host, port, retryCount);
                if (retryCount < 5) {
                    RetryExecutor.executeAsync("netty-connect", () -> doConnect(host, port, retryCount + 1));
                }
                return;
            }
            notifyConnect(future, host, port);
        });
    }

    private void notifyConnect(ChannelFuture future, String host, int port) {
        String endpoint = host + ":" + port;
        channel = future.channel();
        channelMap.put(endpoint, channel);
        logger.info("netty client connect {}:{} success", host, port);
    }

    @Override
    public void disconnect(String endpoint) {
        Channel ch = getChannel(endpoint);
        channelMap.remove(endpoint);
        doDisconnect(ch, endpoint, 0);
    }

    private void doDisconnect(Channel ch, String endpoint, int retryCount) {
        if (ch != null) {
            ch.close().addListener((ChannelFuture future) -> {
                if (!future.isSuccess()) {
                    logger.error("netty client disconnect {} failed, retryCount={}", endpoint, retryCount);
                    if (retryCount < 5) {
                        RetryExecutor.executeAsync("netty-connect", () -> doDisconnect(ch, endpoint, retryCount + 1));
                    }
                    return;
                }
                notifyDisconnect(endpoint);
            });

        }
    }

    private void notifyDisconnect(String endpoint) {
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