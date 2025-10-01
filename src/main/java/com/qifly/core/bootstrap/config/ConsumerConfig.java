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

    public ConsumerConfig(Class<?> serviceInterface) {
        this(serviceInterface, null);
    }

    public ConsumerConfig(Class<?> serviceInterface, List<String> endpoints) {
        this.serviceInterface = serviceInterface;
        this.endpoints = endpoints;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }
}
