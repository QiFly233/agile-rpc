package com.qifly.core.bootstrap.config;

import java.util.List;

/**
 * 消费者配置
 */
public class ConsumerConfig {

    /**
     * 服务接口
     */
    private final Class<?> serviceInterface;

    /**
     * 直连地址
     */
    private final List<String> endpoints;

    /**
     * 序列化协议类型
     */
    private final int protocolType;

    public ConsumerConfig(Class<?> serviceInterface, int protocolType) {
        this(serviceInterface, null, protocolType);
    }

    public ConsumerConfig(Class<?> serviceInterface, List<String> endpoints, int protocolType) {
        this.serviceInterface = serviceInterface;
        this.endpoints = endpoints;
        this.protocolType = protocolType;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public int getProtocolType() {
        return protocolType;
    }
}
