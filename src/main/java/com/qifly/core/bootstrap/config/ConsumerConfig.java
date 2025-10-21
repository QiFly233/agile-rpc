package com.qifly.core.bootstrap.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 消费者配置
 */
public class ConsumerConfig {

    /**
     * 服务接口
     */
    private Class<?> serviceInterface;

    /**
     * 直连地址
     */
    private List<String> endpoints = new ArrayList<>();

    /**
     * 序列化协议类型
     */
    private int protocolType = 1;

    /**
     * 路由策略
     */
    private String router = "DefaultRouter";

    /**
     * 均衡负载
     */
    private String loadBalance = "RoundRobinLoadBalance";

    /**
     * 注册中心Id
     */
    private String registry;

    public ConsumerConfig() {
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void addEndpoint(String endpoint) {
        this.endpoints.add(endpoint);
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }
}
