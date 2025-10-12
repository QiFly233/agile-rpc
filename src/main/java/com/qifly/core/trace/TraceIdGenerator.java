package com.qifly.core.trace;

import java.util.UUID;

public class TraceIdGenerator {

    /**
     * UUID简单实现，TODO 统一规划
     * @return
     */
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
