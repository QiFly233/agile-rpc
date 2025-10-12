package com.qifly.core.service;

import java.lang.reflect.Method;

public class RpcMethod {

    private final int rpcId;

    private final String methodName;

    private final Method method;

    private final Class<?> reqType;

    private final Class<?> respType;

    public RpcMethod(int rpcId, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        this.rpcId = rpcId;
        this.methodName = method.getName();
        this.method = method;
        this.reqType = parameterTypes[0];
        this.respType = method.getReturnType();;
    }

    public int getRpcId() {
        return rpcId;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getReqType() {
        return reqType;
    }

    public Class<?> getRespType() {
        return respType;
    }

    public String getMethodName() {
        return methodName;
    }
}
