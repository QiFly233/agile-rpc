package com.qifly.core.trace;

public class SpanContext {

    private final String traceId;
    private final String spanId;

    public SpanContext(String traceId, String spanId) {
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }
}
