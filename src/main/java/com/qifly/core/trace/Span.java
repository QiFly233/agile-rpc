package com.qifly.core.trace;

import java.util.LinkedHashMap;
import java.util.Map;

public class Span {
    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private final String name;
    private final long starTimeMs;
    private long endTimeMs;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public Span(String traceId, String spanId, String parentSpanId, String name, long starTimeMs) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.name = name;
        this.starTimeMs = starTimeMs;
    }

    public static Span start(String traceId, String spanId, String parentSpanId, String name) {
        return new Span(traceId, spanId, parentSpanId, name, System.currentTimeMillis());
    }

    public void end() {
        endTimeMs = System.currentTimeMillis();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public String getName() {
        return name;
    }

    public long getStarTimeMs() {
        return starTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String log() {
        return "name=" + name +
                "&traceId=" + traceId +
                "&spanId=" + spanId +
                "&parentSpanId=" + parentSpanId +
                "&start=" + starTimeMs +
                "&end=" + endTimeMs +
                "&cost=" + (endTimeMs - starTimeMs) +
                "&attributes=" + attributes;
    }
}
