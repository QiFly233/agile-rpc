package com.qifly.core.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tracer {

    private static final ThreadLocal<SpanContext> ctx = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(Tracer.class);

    public static String getCurrentTraceId() {
        return getCurrentSpanContext().getTraceId();
    }

    public static SpanContext getCurrentSpanContext() {
        if (ctx.get() == null) {
            String traceId = TraceIdGenerator.generateTraceId();
            String spanId = TraceIdGenerator.generateSpanId();
            ctx.set(new SpanContext(traceId, spanId));
        }
        return ctx.get();
    }

    public static Span start(String name) {
        SpanContext parent = getCurrentSpanContext();
        String traceId = parent.getTraceId();
        String parentSpanId = parent.getSpanId();
        String spanId = TraceIdGenerator.generateSpanId();
        Span span = Span.start(traceId, spanId, parentSpanId, name);
        ctx.set(new SpanContext(traceId, spanId));
        return span;
    }

    public static Span startServerSpan(String traceId,
                                       String parentSpanId,
                                       String name) {
        String spanId = TraceIdGenerator.generateSpanId();
        Span span = Span.start(traceId, spanId, parentSpanId, name);
        ctx.set(new SpanContext(traceId, spanId));
        return span;
    }

    public static void end(Span span) {
        span.end();
        logger.info("span:{}", span.log());
    }
}
