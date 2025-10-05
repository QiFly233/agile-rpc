package com.qifly.core.executors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcThreadPoolExecutors {

    private static final ThreadPoolExecutor serverExecutor;

    private static final ScheduledThreadPoolExecutor retryExecutor;

    static  {
        serverExecutor = new ThreadPoolExecutor(
                2,
                4,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new RpcThreadFactory("server", false),
                new RpcDefaultRejectedHandler("server")
        );
        retryExecutor = new ScheduledThreadPoolExecutor(
                1,
                new RpcThreadFactory("retry", false),
                new RpcDefaultRejectedHandler("retry")
        );
    }

    public static ThreadPoolExecutor getServerExecutor() {
        return serverExecutor;
    }

    public static ScheduledThreadPoolExecutor getRetryExecutor() {
        return retryExecutor;
    }
}
