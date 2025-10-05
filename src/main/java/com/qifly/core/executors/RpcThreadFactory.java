package com.qifly.core.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcThreadFactory implements ThreadFactory {

    Logger logger = LoggerFactory.getLogger(RpcThreadFactory.class);

    private final String prefix;
    private final AtomicInteger seq = new AtomicInteger(1);
    private final boolean isDaemon;

    public RpcThreadFactory(String prefix, boolean isDaemon) {
        this.prefix = prefix;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "rpc-thread-" + prefix + "-" + seq.getAndIncrement());
        thread.setDaemon(isDaemon);
        thread.setUncaughtExceptionHandler((t, e) ->
                logger.error("{} thread uncaught ex thread", t.getName(), e));
        return thread;
    }
}
