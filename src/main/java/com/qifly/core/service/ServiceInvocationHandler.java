package com.qifly.core.service;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.discovery.Discovery;
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

    private final Discovery discovery;

    public ServiceInvocationHandler(Consumer consumer, TransportClient client, Discovery discovery) {
        this.consumer = consumer;
        this.client = client;
        this.discovery = discovery;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (consumer.getRpcId(method) <= 0) {
            return method.invoke(this, args);
        }
        Message req = (Message) args[0];
        int rpcId = consumer.getRpcId(method);
        RpcBody reqBody = RpcBody.newBuilder()
                .setRpcId(rpcId)
                .setData(Any.pack(req)).build();

        // TODO 策略
        String endpoint = null;
        if (consumer.getEndpoints() != null && !consumer.getEndpoints().isEmpty()) {
            endpoint = consumer.getEndpoints().get(0);
        } else if (discovery != null) {
            endpoint = discovery.discover(consumer.getServiceName());
        }

        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        CompletableFuture<Any> future = client.send(endpoint, reqBody);
        return future.thenApply(respBody -> {
            try {
                return respBody.unpack(consumer.getRespType(method));
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }
}
