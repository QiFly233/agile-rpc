package com.qifly.core.bootstrap.config;

/**
 * 提供者配置
 */
public class ProviderConfig {

    /**
     * 服务接口
     */
    private final Class<?> serviceInterface;

    /**
     * 服务具体实现
     */
    private final Object serviceImpl;

    /**
     * 端口
     */
    private final int port;

    /**
     * 序列化协议类型
     */
    private final int protocolType;

    public ProviderConfig(Class<?> serviceInterface, Object serviceImpl, int port, int protocolType) {
        this.serviceInterface = serviceInterface;
        this.serviceImpl = serviceImpl;
        this.port = port;
        this.protocolType = protocolType;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public int getPort() {
        return port;
    }

    public int getProtocolType() {
        return protocolType;
    }
}
