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
    private final int type;

    public RegistryConfig(String baseUrl, int type) {
        this.baseUrl = baseUrl;
        this.type = type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getType() {
        return type;
    }
}
