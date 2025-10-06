package com.qifly.core.retry;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RetryExecutorTest {

    @Test
    public void testExecuteSyncSuccess() {
        String result = RetryExecutor.executeSync("successTask", () -> "OK");
        assertEquals("OK", result);
    }

    @Test
    public void testExecuteSyncRetryThenSuccess() {
        AtomicInteger cnt = new AtomicInteger();
        Callable<String> task = () -> {
            int c = cnt.incrementAndGet();
            if (c < 3) {
                throw new RuntimeException("fail " + c);
            }
            return "OK";
        };
        String result = RetryExecutor.executeSync("retryThenSuccess", task);
        assertEquals("OK", result);

    }

    @Test
    public void testExecuteSyncAlwaysFail() {
        Callable<String> task = () -> {
            throw new RuntimeException("always fail");
        };
        String result = RetryExecutor.executeSync("alwaysFail", task);
        assertNull(result);
    }

    @Test
    public void testExecuteAsync() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        RetryExecutor.executeAsync("asyncRetry", () -> {
            int c = counter.incrementAndGet();
            if (c < 3) {
                throw new RuntimeException("fail " + c);
            }
            latch.countDown();
        });
        latch.await(5_000, java.util.concurrent.TimeUnit.MILLISECONDS);
        assertEquals(3, counter.get());
    }
}
