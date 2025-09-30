package com.qifly.core.bootstrap.config;

/**
 * 服务注册配置
 */
public class RegisterServiceConfig {

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

    public RegisterServiceConfig(Class<?> serviceInterface, int port) {
        this(serviceInterface, null, port);
    }

    public RegisterServiceConfig(Class<?> serviceInterface) {
        this(serviceInterface, null, 0);
    }

    public RegisterServiceConfig(Class<?> serviceInterface, Object serviceImpl, int port) {
        this.serviceInterface = serviceInterface;
        this.serviceImpl = serviceImpl;
        this.port = port;
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
}
