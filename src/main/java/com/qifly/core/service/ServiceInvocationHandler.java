package com.qifly.core.service;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.cluster.Cluster;
import com.qifly.core.protocol.data.RpcBody;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 远程调用代理本地调用
 */
public class ServiceInvocationHandler implements InvocationHandler {

    Logger logger = LoggerFactory.getLogger(ServiceInvocationHandler.class);

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
        String endpoint = cluster.getEndpoint(consumer.getServiceName());
        if (endpoint == null) {
            logger.error("endpoint is null");
            return null;
        }
        int rpcId = consumer.getRpcId(method);
        CompletableFuture<RpcFrame> future;
        if (consumer.getProtocolType() == 1) {
            Message req = (Message) args[0];
            RpcBody reqBody = RpcBody.newBuilder()
                    .setRpcId(rpcId)
                    .setData(Any.pack(req))
                    .build();
            future = client.send(endpoint, reqBody.toByteArray(), consumer.getProtocolType());
        }
        else {
            logger.error("unsupported protocol type");
            return null;
        }
        return future.thenApply(rpcFrame -> {
            if (rpcFrame.getProtocolType() != consumer.getProtocolType()) {
                logger.error("inconsistent protocol type");
                return null;
            }
            if (rpcFrame.getStatus() != 0) {
                logger.error("server response error, status:{}", rpcFrame.getStatus());
                return null;
            }
            byte[] bytes = rpcFrame.getBody();
            if (rpcFrame.getProtocolType() == 1) {
                try {
                    RpcBody rpcBody = RpcBody.parseFrom(bytes);
                    Any any = rpcBody.getData();
                    return any.unpack(consumer.getRespType(method));
                } catch (InvalidProtocolBufferException e) {
                    logger.error("protocol parse error", e);
                    return null;
                }
            }
            return null;
        }).get();
    }
}
