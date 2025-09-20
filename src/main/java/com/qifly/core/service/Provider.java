package com.qifly.core.service;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.qifly.core.utils.ServiceUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产者
 */
public class Provider {

    /**
     * 服务接口
     */
    private final Class<?> itf;

    /**
     * 服务具体实现
     */
    private final Object impl;

    /**
     * 方法映射
     */
    private final Map<Integer, RpcMethod> methodMap;

    /**
     * 端口
     */
    private final int port;

    public Provider(Class<?> itf, Object impl, int port) {
        this.itf = itf;
        this.impl = impl;
        this.port = port;
        methodMap = getMethods();
    }

    public Map<Integer, RpcMethod> getMethods() {
        Map<Integer, RpcMethod> methodMap = new HashMap<>();
        Method[] methods = itf.getMethods();
        for (Method method : methods) {
            int rpcId = ServiceUtil.getRpcId(itf, method);
            if (rpcId <= 0) {
                continue;
            }
            RpcMethod rpcMethod = new RpcMethod(rpcId, method);
            methodMap.put(rpcId, rpcMethod);
        }
        return methodMap;
    }

    public Object getImpl() {
        return impl;
    }

    public Message invokeMethod(int rpcId, Any any) {
        RpcMethod rpcMethod = methodMap.get(rpcId);
        Method method = rpcMethod.getMethod();
        try {
            Message req = any.unpack(rpcMethod.getReqType());
            return (Message) method.invoke(impl, req);
        } catch (Exception e) {
            return null;
        }
    }

    public RpcMethod getRpcMethod(int rpcId) {
        return methodMap.get(rpcId);
    }

    public Method getMethod(int rpcId) {
        if (methodMap.containsKey(rpcId)) {
            return methodMap.get(rpcId).getMethod();
        }
        return null;
    }

    public int getPort() {
        return port;
    }
}
