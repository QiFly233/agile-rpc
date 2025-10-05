package com.qifly.core.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcDefaultRejectedHandler implements RejectedExecutionHandler {

    Logger logger = LoggerFactory.getLogger(RpcDefaultRejectedHandler.class);

    private final String name;

    public RpcDefaultRejectedHandler(String name) {
        this.name = name;
    }


    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.error("rpc {} pool rejected, active={}, queue size={}", name, executor.getActiveCount(), executor.getQueue().size());
        throw new RejectedExecutionException("rpc " + name + " pool rejected");
    }
}
