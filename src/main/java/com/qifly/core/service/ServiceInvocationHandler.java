package com.qifly.core.service;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.transport.TransportClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 远程调用代理本地调用
 */
public class ServiceInvocationHandler implements InvocationHandler {

    private final Consumer consumer;

    private final TransportClient client;

    public ServiceInvocationHandler(Consumer consumer, TransportClient client) {
        this.consumer = consumer;
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (consumer.getRpcId(method) <= 0) {
            return method.invoke(this, args);
        }
        Message req = (Message) args[0];
        CompletableFuture<Any> future = client.send(consumer.getRpcId(method), req);
        return future.thenApply(body -> {
            try {
                return body.unpack(consumer.getRespType(method));
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }
}
