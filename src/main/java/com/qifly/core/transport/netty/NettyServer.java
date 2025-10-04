package com.qifly.core.transport.netty;

import com.qifly.core.exception.TransportException;
import com.qifly.core.protocol.frame.FrameCodec;
import com.qifly.core.service.Provider;
import com.qifly.core.transport.TransportServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class NettyServer implements TransportServer {

    Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final int port;
    private final Provider provider;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ServerBootstrap bootstrap;
    private volatile Channel serverChannel;
    private final int maxFrameLength = 1024 * 1024 + 16;

    public NettyServer(int port, Provider provider) {
        this.port = port;
        this.provider = provider;
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 4, 4, 8, 0));
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 180));
                        ch.pipeline().addLast(new FrameCodec());
                        ch.pipeline().addLast(new ServerHandler(provider));
                    }
                });
    }

    @Override
    public void start() throws TransportException, InterruptedException {
        ChannelFuture future = bootstrap.bind(port).sync();
        if (!future.isSuccess()) {
            throw new TransportException("netty server bind port:" + port + "failed", future.cause());
        }
        serverChannel = future.channel();
        logger.info("netty server started on port {}", port);
    }

    @Override
    public void close() {
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}