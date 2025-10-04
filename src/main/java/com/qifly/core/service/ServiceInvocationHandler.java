package com.qifly.core.service;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.cluster.Cluster;
import com.qifly.core.protocol.data.RpcBody;
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

    private final Cluster cluster;

    public ServiceInvocationHandler(Consumer consumer, TransportClient client, Cluster cluster) {
        this.consumer = consumer;
        this.client = client;
        this.cluster = cluster;
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

        String endpoint = cluster.getEndpoint(consumer.getServiceName());
        if (endpoint == null) {
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
