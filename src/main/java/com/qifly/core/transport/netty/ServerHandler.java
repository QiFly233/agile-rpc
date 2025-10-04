package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.data.RpcStatusCode;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.service.Provider;
import com.qifly.core.service.RpcMethod;
import com.qifly.core.utils.RpcBodyUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
                if (msg.getProtocolType() != provider.getProtocolType()) {
                    ctx.writeAndFlush(RpcFrame.response(msg, (byte) 100, Unpooled.EMPTY_BUFFER));
                }
                byte[] bodyBytes;
                if (msg.getProtocolType() == 1) {
                    RpcBody respBody;
                    try {
                        RpcBody reqBody = RpcBodyUtil.parseFrom(msg.getBody());
                        respBody = invokeMethod(reqBody);
                    } catch (InvalidProtocolBufferException e) {
                        respBody = RpcBody.newBuilder()
                                .setStatusCode(RpcStatusCode.RPC_METHOD_REQUEST_ERROR)
                                .build();
                    }
                    bodyBytes = respBody.toByteArray();
                } else {
                    logger.error("unsupported protocol type");
                    return;
                }
                ctx.writeAndFlush(RpcFrame.response(msg, (byte) 0, Unpooled.wrappedBuffer(bodyBytes)));
            }
        } catch (Exception e) {
            RpcBody respBody = RpcBody.newBuilder()
                    .setStatusCode(RpcStatusCode.RPC_UNKNOWN_ERROR)
                    .build();
            ctx.writeAndFlush(RpcFrame.response(msg, (byte) 0, Unpooled.wrappedBuffer(respBody.toByteArray())));
        }
    }


    private RpcBody invokeMethod(RpcBody reqBody) {
        RpcMethod rpcMethod = provider.getRpcMethod(reqBody.getRpcId());
        if (rpcMethod == null || rpcMethod.getMethod() == null) {
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_METHOD_NOT_FOUND_ERROR)
                    .build();
        }

        Message req;
        try {
            Any any = reqBody.getData();
            req = any.unpack(rpcMethod.getReqType());
        } catch (InvalidProtocolBufferException e) {
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_METHOD_REQUEST_ERROR)
                    .build();
        }

        Method method = rpcMethod.getMethod();
        try {
            Message resp = (Message) method.invoke(provider.getImpl(), req);
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setData(Any.pack(resp))
                    .build();
        } catch (InvocationTargetException | IllegalAccessException e) {
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_METHOD_NOT_FOUND_ERROR)
                    .build();
        }
    }
}