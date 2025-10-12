package com.qifly.core.transport.context;

import com.qifly.core.trace.Span;
import io.netty.channel.ChannelHandlerContext;

public class RpcServerContext {
    private final ChannelHandlerContext channelHandlerContext;
    private final Span span;
    private final byte[] reqBody;
    private byte[] respBody;
    private final long requestId;


    public RpcServerContext(ChannelHandlerContext channelHandlerContext, Span span, byte[] reqBody, long requestId) {
        this.channelHandlerContext = channelHandlerContext;
        this.span = span;
        this.reqBody = reqBody;
        this.requestId = requestId;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public Span getSpan() {
        return span;
    }

    public byte[] getReqBody() {
        return reqBody;
    }

    public byte[] getRespBody() {
        return respBody;
    }

    public void setRespBody(byte[] respBody) {
        this.respBody = respBody;
    }

    public long getRequestId() {
        return requestId;
    }
}
