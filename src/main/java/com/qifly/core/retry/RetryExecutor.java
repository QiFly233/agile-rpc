package com.qifly.core.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


/**
 * TODO 后续扩展
 */
public class RetryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

    private static final int maxRetryTimes = 3;

    private static final int retryDelayMs = 1000;

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }


    public static void execute(String taskName, ThrowingRunnable task) {
        innerRetry(taskName, () -> {
            task.run();
            return null;
        });
    }

    public static <T> T execute(String taskName, Callable<T> task) {
        return innerRetry(taskName, task);
    }

    public static void executeAsync(String taskName, ThrowingRunnable task) {
        new Thread(() -> {
            execute(taskName, task);
        }).start();
    }

    private static <T> T innerRetry(String taskName, Callable<T> task) {
        for (int i = 1; i <= maxRetryTimes; i++) {
            try {
                return task.call();
            } catch (Exception e) {
                logger.warn("Operation '{}' failed on attempt {}: {}", taskName, i, e.getMessage());
                if (i < maxRetryTimes) {
                    try {
                        Thread.sleep(retryDelayMs * i);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return null;
    }


}
