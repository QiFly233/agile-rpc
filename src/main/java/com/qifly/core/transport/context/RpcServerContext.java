package com.qifly.core.transport.context;

import io.netty.channel.ChannelHandlerContext;

public class RpcServerContext {
    private final ChannelHandlerContext channelHandlerContext;
    private final Object reqBody;
    private final int protocolType;
    private final long requestId;

    public RpcServerContext(ChannelHandlerContext channelHandlerContext, Object reqBody, int protocolType, long requestId) {
        this.channelHandlerContext = channelHandlerContext;
        this.reqBody = reqBody;
        this.protocolType = protocolType;
        this.requestId = requestId;
    }

    public <T> T getReqBody() {
        return (T) reqBody;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public long getRequestId() {
        return requestId;
    }
}
