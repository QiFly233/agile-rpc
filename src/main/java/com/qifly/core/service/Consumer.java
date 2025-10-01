package com.qifly.core.service;

import com.google.protobuf.Message;
import com.qifly.core.utils.ServiceUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者
 */
public class Consumer {

    /**
     * 服务接口
     */
    private final Class<?> itf;

    /**
     * 接口名
     */
    private final String itfName;

    /**
     * 服务名
     */
    private final String serviceName;

    /**
     * 方法与RpcId的映射
     */
    private final Map<Method, RpcMethod> methodMap;

    /**
     * 直连地址
     */
    private final List<String> endpoints;

    public Consumer(Class<?> itf, List<String> endpoints) {
        this.itf = itf;
        itfName = itf.getName();
        serviceName = itf.getSimpleName();
        this.endpoints = endpoints;
        methodMap = getMethods();
    }

    private Map<Method, RpcMethod> getMethods() {
        Map<Method, RpcMethod> methodMap = new HashMap<>();
        Method[] methods = itf.getMethods();
        for (Method method : methods) {
            int rpcId = ServiceUtil.getRpcId(itf, method);
            if (rpcId <= 0) {
                continue;
            }
            RpcMethod rpcMethod = new RpcMethod(rpcId, method);
            methodMap.put(method, rpcMethod);
        }
        return methodMap;
    }

    public Class<?> getItf() {
        return itf;
    }

    public int getRpcId(Method method) {
        if (methodMap.containsKey(method)) {
            return methodMap.get(method).getRpcId();
        }
        return 0;
    }

    public Class<? extends Message> getRespType(Method method) {
        if (methodMap.containsKey(method)) {
            return methodMap.get(method).getRespType();
        }
        return null;
    }

    public String getItfName() {
        return itfName;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public String getServiceName() {
        return serviceName;
    }
}
