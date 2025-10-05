package com.qifly.core.retry;

import com.qifly.core.executors.RpcThreadPoolExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RetryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

    private static final int maxRetryTimes = 3;

    private static final int retryDelayMs = 1000;

    private static final ScheduledThreadPoolExecutor retryExecutor = RpcThreadPoolExecutors.getRetryExecutor();

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static <T> T executeSync(String taskName, Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        scheduleRetry(taskName, task, future, 1);
        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    public static void executeAsync(String taskName, ThrowingRunnable task) {
        scheduleRetry(taskName, () -> {
            task.run();
            return null;
        }, null, 1);
    }

    private static <T> void scheduleRetry(String taskName,
                                            Callable<T> task,
                                            CompletableFuture<T> future,
                                            int attempt) {
        retryExecutor.schedule(() -> runAttempt(taskName, task, future, attempt), retryDelayMs, TimeUnit.MILLISECONDS);
    }

    private static <T> void runAttempt(String taskName,
                                       Callable<T> task,
                                       CompletableFuture<T> future,
                                       int attempt) {
        try {
            T result = task.call();
            if (future != null && !future.isDone()) {
                future.complete(result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Operation '{}' interrupted on attempt {}", taskName, attempt, e);
            if (future != null && !future.isDone()) {
                future.completeExceptionally(e);
            }
        } catch (Exception e) {
            logger.error("Operation '{}' failed on attempt {}", taskName, attempt, e);
            if (attempt < maxRetryTimes) {
                scheduleRetry(taskName, task, future, attempt + 1);
            } else {
                if (future != null && !future.isDone()) {
                    future.completeExceptionally(e);
                }
            }
        }
    }

}
