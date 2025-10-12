package com.qifly.core.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class RpcThreadPoolExecutors {

    private static final Logger logger = LoggerFactory.getLogger(RpcThreadPoolExecutors.class);

    private static final ThreadPoolExecutor serverExecutor;

    private static final ScheduledThreadPoolExecutor retryExecutor;

    private static final ExecutorService discoveryExecutor = Executors.newCachedThreadPool();;

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

    public static void shutdownServerExecutor() {
        serverExecutor.shutdown();
        try {
            if (!serverExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                serverExecutor.shutdownNow();
                if (!serverExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.warn("server executor did not terminate");
                }
            }
        } catch (InterruptedException ignored) {

        }
    }

    public static ScheduledThreadPoolExecutor getRetryExecutor() {
        return retryExecutor;
    }

    public static ExecutorService getDiscoveryExecutor() {
        return discoveryExecutor;
    }

    public static void shutdownDiscoveryExecutor() {
        discoveryExecutor.shutdownNow();
    }
}
