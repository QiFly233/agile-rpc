package com.qifly.core.bootstrap;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.config.RegisterServiceConfig;
import com.qifly.core.bootstrap.config.RegistryConfig;
import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;

import java.util.ArrayList;
import java.util.List;

public class RpcBootstrap {

    /**
     * 生产者服务配置
     */
    RegisterServiceConfig providerConfig;

    /**
     * 消费者服务配置
     */
    List<RegisterServiceConfig> consumerConfigs = new ArrayList<>();

    /**
     * 注册中心配置
     */
    RegistryConfig registryConfig;

    public RpcBootstrap() {

    }

    public RpcBootstrap addProvider(Class<?> serviceInterface, Object serviceImpl, int port) {
        providerConfig = new RegisterServiceConfig(serviceInterface, serviceImpl, port);
        return this;
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface, int port) {
        consumerConfigs.add(new RegisterServiceConfig(serviceInterface, port));
        return this;
    }

    public RpcBootstrap addRegister(String address) {
        registryConfig = new RegistryConfig(address);
        return this;
    }

    public RpcApp build() {
        RpcApp rpcApp = new RpcApp();

        if (providerConfig != null) {
            Provider provider = new Provider(providerConfig.getServiceInterface(), providerConfig.getServiceImpl(), providerConfig.getPort());
            rpcApp.setProvider(provider);
        }

        List<Consumer> consumers = new ArrayList<>();
        for (RegisterServiceConfig c : consumerConfigs) {
            Consumer consumer = new Consumer(c.getServiceInterface(), c.getPort());
            consumers.add(consumer);
        }
        rpcApp.setConsumers(consumers);
        rpcApp.init();
        return rpcApp;
    }

}
