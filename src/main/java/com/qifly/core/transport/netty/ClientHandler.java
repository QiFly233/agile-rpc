package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.frame.RpcFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

class ClientHandler extends SimpleChannelInboundHandler<RpcFrame> {

    Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final ConcurrentMap<Long, CompletableFuture<Any>> futureMap;

    ClientHandler(ConcurrentMap<Long, CompletableFuture<Any>> futureMap) {
        this.futureMap = futureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcFrame msg) {
        long requestId = msg.getRequestId();
        if (!msg.isRequest()) {
            CompletableFuture<Any> future = futureMap.remove(requestId);
            if (future == null) {
                return;
            }
            ByteBuf byteBuf = msg.getBody();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            try {
                RpcBody body = RpcBody.parseFrom(bytes);
                future.complete(body.getData());
            } catch (Exception e) {

            }
        }
        String text = msg.getBody().toString(CharsetUtil.UTF_8);
        logger.info("netty client receive text={}", text);
    }
}