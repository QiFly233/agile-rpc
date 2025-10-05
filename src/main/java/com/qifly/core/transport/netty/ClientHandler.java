package com.qifly.core.transport.netty;

import com.qifly.core.protocol.frame.RpcFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

public class ClientHandler extends SimpleChannelInboundHandler<RpcFrame> {

    Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final ConcurrentMap<Long, CompletableFuture<RpcFrame>> futureMap;

    ClientHandler(ConcurrentMap<Long, CompletableFuture<RpcFrame>> futureMap) {
        this.futureMap = futureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcFrame msg) {
        long requestId = msg.getRequestId();
        if (!msg.isRequest()) {
            CompletableFuture<RpcFrame> future = futureMap.remove(requestId);
            if (future == null) {
                return;
            }
            future.complete(msg);
        }
    }
}