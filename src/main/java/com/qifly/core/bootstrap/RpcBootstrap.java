package com.qifly.core.bootstrap;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.config.RegisterServiceConfig;
import com.qifly.core.bootstrap.config.RegistryConfig;
import com.qifly.core.registry.ConsulRegistry;
import com.qifly.core.registry.DefaultDiscovery;
import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;
import com.qifly.core.transport.netty.NettyClient;
import com.qifly.core.transport.netty.NettyServer;

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

    // TODO 修改为直连
    public RpcBootstrap addConsumer(Class<?> serviceInterface, int port) {
        consumerConfigs.add(new RegisterServiceConfig(serviceInterface, port));
        return this;
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface) {
        consumerConfigs.add(new RegisterServiceConfig(serviceInterface));
        return this;
    }

    public RpcBootstrap addRegister(String baseUrl, int type) {
        registryConfig = new RegistryConfig(baseUrl, type);
        return this;
    }

    public RpcApp build() {
        RpcApp rpcApp = new RpcApp();

        if (providerConfig != null) {
            Provider provider = new Provider(providerConfig.getServiceInterface(), providerConfig.getServiceImpl(), providerConfig.getPort());
            rpcApp.setProvider(provider);
            NettyServer nettyServer = new NettyServer(provider.getPort(), provider);
            rpcApp.setServer(nettyServer);
        }

        if (consumerConfigs != null && !consumerConfigs.isEmpty()) {
            List<Consumer> consumers = new ArrayList<>();
            for (RegisterServiceConfig c : consumerConfigs) {
                Consumer consumer = new Consumer(c.getServiceInterface(), c.getPort());
                consumers.add(consumer);
            }
            rpcApp.setConsumers(consumers);
            NettyClient nettyClient = new NettyClient();
            rpcApp.setClient(nettyClient);
        }


        if (registryConfig != null) {
            if (registryConfig.getType() == 1) {
                rpcApp.setDiscovery(new DefaultDiscovery(new ConsulRegistry(registryConfig.getBaseUrl()), rpcApp.getProvider(), rpcApp.getConsumers(), rpcApp.getClient()));
            }
        }

        rpcApp.init();
        return rpcApp;
    }

}
