package com.qifly.core.bootstrap.config;

/**
 * 注册中心配置
 */
public class RegistryConfig {

    /**
     * 唯一键
     */
    private String id;

    /**
     * 地址
     */
    private String baseUrl;

    /**
     * 注册中心类型
     */
    private String registry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
