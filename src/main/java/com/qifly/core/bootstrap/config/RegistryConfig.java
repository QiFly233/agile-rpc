package com.qifly.core.bootstrap.config;

/**
 * 注册中心配置
 */
public class RegistryConfig {

    /**
     * 地址
     */
    private final String baseUrl;

    /**
     * 注册中心类型， 1=consul
     */
    private final String name;

    public RegistryConfig(String baseUrl, String name) {
        this.baseUrl = baseUrl;
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getName() {
        return name;
    }
}
