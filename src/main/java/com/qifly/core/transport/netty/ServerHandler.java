package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.service.Provider;
import com.qifly.core.service.RpcMethod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerHandler extends SimpleChannelInboundHandler<RpcFrame> {

    Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Provider provider;

    public ServerHandler(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcFrame msg) {
        try {
            if (msg.isRequest()) {
                ByteBuf byteBuf = msg.getBody();
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.getBytes(byteBuf.readerIndex(), bytes);
                RpcBody body = RpcBody.parseFrom(bytes);
                logger.debug("netty server receive body:{}", body);
                RpcMethod rpcMethod = provider.getRpcMethod(body.getRpcId());
                Message resp = provider.invokeMethod(rpcMethod.getRpcId(), body.getData());
                RpcBody respBody = RpcBody.newBuilder()
                        .setRpcId(body.getRpcId())
                        .setData(Any.pack(resp))
                        .build();
                ctx.writeAndFlush(RpcFrame.response(msg, (byte) 0, Unpooled.wrappedBuffer(respBody.toByteArray())));
            }
        } catch (Exception e) {
            ctx.writeAndFlush(RpcFrame.response(msg, (byte) 0, Unpooled.copiedBuffer("1", CharsetUtil.UTF_8)));
        }
    }
}