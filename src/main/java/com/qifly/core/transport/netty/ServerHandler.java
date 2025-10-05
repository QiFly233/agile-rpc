package com.qifly.core.transport.netty;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.executors.RpcThreadPoolExecutors;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.data.RpcStatusCode;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.protocol.frame.RpcFrameStatus;
import com.qifly.core.service.Provider;
import com.qifly.core.service.RpcMethod;
import com.qifly.core.transport.context.RpcServerContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerHandler extends SimpleChannelInboundHandler<RpcFrame> {

    Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Provider provider;

    private final ThreadPoolExecutor executor = RpcThreadPoolExecutors.getServerExecutor();

    public ServerHandler(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcFrame msg) {
        int protocolType = msg.getProtocolType();
        long requestId = msg.getRequestId();

        if (msg.isRequest()) {
            if (protocolType != provider.getProtocolType()) {
                ctx.writeAndFlush(
                        RpcFrame.error(protocolType, false, requestId, RpcFrameStatus.INCONSISTENT_BODY_PROTOCOL)
                );
            }
            RpcServerContext rpcServerContext;
            if (protocolType == 1) {
                try {
                    RpcBody reqBody = RpcBody.parseFrom(msg.getBody());
                    rpcServerContext = new RpcServerContext(ctx, reqBody, protocolType, requestId);
                } catch (InvalidProtocolBufferException e) {
                    RpcBody respBody = RpcBody.newBuilder()
                            .setStatusCode(RpcStatusCode.RPC_METHOD_REQUEST_ERROR)
                            .build();
                    ctx.writeAndFlush(
                            RpcFrame.ok(protocolType, false, requestId, respBody.toByteArray())
                    );
                    return;
                }
            } else {
                logger.error("unsupported protocol type");
                ctx.writeAndFlush(
                        RpcFrame.error(protocolType, false, requestId, RpcFrameStatus.UNSUPPORTED_BODY_PROTOCOL)
                );
                return;
            }
            handle(rpcServerContext);
        }

    }

    private void handle(RpcServerContext ctx) {
        ChannelHandlerContext channelHandlerContext = ctx.getChannelHandlerContext();
        if (executor != null) {
            executor.execute(() -> {
                RpcBody respBody = invokeMethod(ctx.getReqBody());
                logger.debug("pool invoke method, reqBOdy:{}, respBody:{}", ctx.getReqBody(), respBody);
                channelHandlerContext.writeAndFlush(
                        RpcFrame.ok(ctx.getProtocolType(), false, ctx.getRequestId(), respBody.toByteArray())
                );
            });
        }
        else {
            RpcBody respBody = invokeMethod(ctx.getReqBody());
            logger.debug("invoke method, reqBody:{}, respBody:{}", ctx.getReqBody(), respBody);
            channelHandlerContext.writeAndFlush(
                    RpcFrame.ok(ctx.getProtocolType(), false, ctx.getRequestId(), respBody.toByteArray())
            );
        }
    }


    private RpcBody invokeMethod(RpcBody reqBody) {
        try {
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
                        .setStatusCode(RpcStatusCode.RPC_SUCCESS)
                        .setData(Any.pack(resp))
                        .build();
            } catch (InvocationTargetException | IllegalAccessException e) {
                return RpcBody.newBuilder()
                        .setRpcId(reqBody.getRpcId())
                        .setStatusCode(RpcStatusCode.RPC_METHOD_NOT_FOUND_ERROR)
                        .build();
            }
        } catch (Exception e) {
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_UNKNOWN_ERROR)
                    .build();
        }
    }
}