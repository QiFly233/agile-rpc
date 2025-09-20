package com.qifly.core.service;

import com.google.protobuf.Message;

import java.lang.reflect.Method;

public class RpcMethod {

    private final int rpcId;

    private final Method method;

    private final Class<? extends Message> reqType;

    private final Class<? extends Message> respType;

    public RpcMethod(int rpcId, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        @SuppressWarnings("unchecked")
        Class<? extends Message> reqType = (Class<? extends Message>) parameterTypes[0];
        @SuppressWarnings("unchecked")
        Class<? extends Message> respType = (Class<? extends Message>) returnType;
        this.rpcId = rpcId;
        this.method = method;
        this.reqType = reqType;
        this.respType = respType;
    }

    public int getRpcId() {
        return rpcId;
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends Message> getReqType() {
        return reqType;
    }

    public Class<? extends Message> getRespType() {
        return respType;
    }
}
