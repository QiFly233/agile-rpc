package com.qifly.core.bootstrap.config;

/**
 * 提供者配置
 */
public class ProviderConfig {

    /**
     * 服务接口
     */
    private Class<?> serviceInterface;

    /**
     * 服务具体实现
     */
    private Object serviceImpl;

    /**
     * 端口
     */
    private int port;

    /**
     * 序列化协议类型
     */
    private int protocolType;

    /**
     * 注册中心Id
     */
    private String registry;

    public ProviderConfig() {
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }
}
