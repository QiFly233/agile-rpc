package com.qifly.core.service;

import com.qifly.core.cluster.Cluster;
import com.qifly.core.exception.RpcException;
import com.qifly.core.protocol.data.RpcBodyHandler;
import com.qifly.core.protocol.data.RpcBodyHandlerFactory;
import com.qifly.core.protocol.frame.RpcFrame;
import com.qifly.core.protocol.frame.meta.RpcMetaData;
import com.qifly.core.protocol.frame.meta.Trace;
import com.qifly.core.trace.Span;
import com.qifly.core.trace.Tracer;
import com.qifly.core.transport.TransportClient;
import com.qifly.core.transport.context.RpcClientContext;
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

    private final RpcBodyHandler handler;

    public ServiceInvocationHandler(Consumer consumer, TransportClient client, Cluster cluster) {
        this.consumer = consumer;
        this.client = client;
        this.cluster = cluster;
        this.handler = RpcBodyHandlerFactory.getHandler(consumer.getProtocolType());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int rpcId = consumer.getRpcId(method);
        String serviceName = consumer.getServiceName();
        String methodName = consumer.getMethodName(method);
        if (rpcId <= 0) {
            return method.invoke(this, args);
        }
        Span span = Tracer.start("rpc.client#" + serviceName + "." + methodName);
        String endpoint = cluster.getEndpoint(serviceName);
        try {
            if (endpoint == null) {
                throw new RpcException("service no connect");
            }
            RpcClientContext ctx = new RpcClientContext(span, args[0]);
            sendAndReceive(method, endpoint, ctx);
            return ctx.getRespBody();
        } catch (RpcException e) {
            span.setAttribute("error", e.getMessage());
            throw e;
        } finally {
            Tracer.end(span);
        }
    }

    private void sendAndReceive(Method method, String endpoint, RpcClientContext ctx) throws Throwable {
        Span span = ctx.getSpan();
        span.setAttribute("req", ctx.getReqBody());
        CompletableFuture<RpcFrame> future = send(method, endpoint, ctx);
        receive(method, ctx, future);
        span.setAttribute("resp", ctx.getRespBody());
    }

    private CompletableFuture<RpcFrame> send(Method method, String endpoint, RpcClientContext ctx) {
        Span span = ctx.getSpan();
        RpcMetaData rpcMetaData = RpcMetaData.newBuilder()
                .setTrace(Trace.newBuilder().setTraceId(span.getTraceId()).setSpanId(span.getSpanId()).build())
                .build();
        byte[] bytes = handler.toReq(consumer.getRpcId(method), ctx.getReqBody());
        return client.send(endpoint, rpcMetaData, bytes, consumer.getProtocolType());
    }

    private void receive(Method method, RpcClientContext ctx, CompletableFuture<RpcFrame> future) throws Throwable {
        RpcFrame rpcFrame = future.get();
        if (rpcFrame.getProtocolType() != consumer.getProtocolType()) {
            throw new RpcException("inconsistent protocol between server and client");
        }
        if (rpcFrame.getStatus() != 0) {
            throw new RpcException("server response error, status=" + rpcFrame.getStatus());
        }
        byte[] resBytes = rpcFrame.getRpcBody();
        Object resp = handler.toResp(resBytes, consumer.getRespType(method));
        ctx.setRespBody(resp);
    }
}
