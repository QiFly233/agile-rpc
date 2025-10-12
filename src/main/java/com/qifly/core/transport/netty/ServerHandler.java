package com.qifly.core.transport.netty;

import com.qifly.core.executors.RpcThreadPoolExecutors;
import com.qifly.core.protocol.data.RpcBodyHandler;
import com.qifly.core.protocol.data.RpcBodyHandlerFactory;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.protocol.frame.RpcFrameStatus;
import com.qifly.core.protocol.frame.meta.RpcMetaData;
import com.qifly.core.protocol.frame.meta.Trace;
import com.qifly.core.service.Provider;
import com.qifly.core.trace.Span;
import com.qifly.core.trace.Tracer;
import com.qifly.core.transport.context.RpcServerContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class ServerHandler extends SimpleChannelInboundHandler<RpcFrame> {

    Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Provider provider;

    private final ThreadPoolExecutor executor = RpcThreadPoolExecutors.getServerExecutor();

    private final RpcBodyHandler handler;

    public ServerHandler(Provider provider) {
        this.provider = provider;
        this.handler = RpcBodyHandlerFactory.getHandler(provider.getProtocolType());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcFrame msg) {
        if (!msg.isRequest()) {
            return;
        }
        int protocolType = msg.getProtocolType();
        long requestId = msg.getRequestId();
        RpcMetaData rpcMetaData = msg.getRpcMetaData();
        String traceId = rpcMetaData.getTrace().getTraceId();
        String spanId = rpcMetaData.getTrace().getSpanId();
        Span span = Tracer.startServerSpan(traceId, spanId, "rpc.server");
        RpcServerContext rpcServerContext = new RpcServerContext(ctx, span, msg.getRpcBody(), requestId);

        if (protocolType != provider.getProtocolType()) {
            send(rpcServerContext, RpcFrameStatus.INCONSISTENT_BODY_PROTOCOL);
            return;
        }
        receive(rpcServerContext);
    }

    private void send(RpcServerContext ctx, byte status) {
        Span span = ctx.getSpan();
        RpcMetaData rpcMetaData = RpcMetaData.newBuilder()
                .setTrace(Trace.newBuilder().setTraceId(span.getTraceId()).setSpanId(span.getSpanId()).build())
                .build();
        ctx.getChannelHandlerContext().writeAndFlush(
                RpcFrame.response(provider.getProtocolType(), false, ctx.getRequestId(), status, rpcMetaData, ctx.getRespBody())
        );
        Tracer.end(span);
    }

    private void receive(RpcServerContext ctx) {
        if (executor != null) {
            executor.execute(() -> invokeMethod(ctx));
        }
        else {
            invokeMethod(ctx);
        }
    }

    private void invokeMethod(RpcServerContext ctx) {
        byte[] respond = handler.respond(provider, ctx.getReqBody());
        ctx.setRespBody(respond);
        send(ctx, RpcFrameStatus.SUCCESS);
    }
}