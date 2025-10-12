package com.qifly.core.transport.context;

import com.qifly.core.trace.Span;

public class RpcClientContext {
    private final Span span;
    private final Object reqBody;
    private Object respBody;

    public RpcClientContext(Span span, Object reqBody) {
        this.span = span;
        this.reqBody = reqBody;
    }

    public Span getSpan() {
        return span;
    }

    public Object getReqBody() {
        return reqBody;
    }

    public Object getRespBody() {
        return respBody;
    }

    public void setRespBody(Object respBody) {
        this.respBody = respBody;
    }
}
