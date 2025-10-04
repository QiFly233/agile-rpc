package com.qifly.core.service;

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
     * 服务名
     */
    private final String serviceName;

    /**
     * 方法映射
     */
    private final Map<Integer, RpcMethod> methodMap;

    /**
     * 端口
     */
    private final int port;

    /**
     * 序列化协议
     */
    private final int protocolType;

    public Provider(Class<?> itf, Object impl, int port, int protocolType) {
        this.itf = itf;
        this.impl = impl;
        this.port = port;
        serviceName = itf.getSimpleName();
        this.protocolType = protocolType;
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

    public String getServiceName() {
        return serviceName;
    }

    public int getProtocolType() {
        return protocolType;
    }
}
