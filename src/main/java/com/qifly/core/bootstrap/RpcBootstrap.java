package com.qifly.core.bootstrap;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.config.ConsumerConfig;
import com.qifly.core.bootstrap.config.ProviderConfig;
import com.qifly.core.bootstrap.config.RegistryConfig;
import com.qifly.core.discovery.DefaultDiscovery;
import com.qifly.core.discovery.Discovery;
import com.qifly.core.discovery.NoRegistryDiscovery;
import com.qifly.core.discovery.registry.Registry;
import com.qifly.core.loader.SpiLoader;
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
    ProviderConfig providerConfig;

    /**
     * 消费者服务配置
     */
    List<ConsumerConfig> consumerConfigs = new ArrayList<>();

    /**
     * 注册中心配置
     */
    RegistryConfig registryConfig;

    public RpcBootstrap addProvider(Class<?> serviceInterface, Object serviceImpl, int port) {
        return addProvider(serviceInterface, serviceImpl, port, 1);
    }

    public RpcBootstrap addProvider(Class<?> serviceInterface, Object serviceImpl, int port, int protocolType) {
        providerConfig = new ProviderConfig(serviceInterface, serviceImpl, port, protocolType);
        return this;
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface, List<String> endpoints) {
        return addConsumer(serviceInterface, endpoints, 1);
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface, List<String> endpoints, int protocolType) {
        consumerConfigs.add(new ConsumerConfig(serviceInterface, endpoints, protocolType));
        return this;
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface) {
        return addConsumer(serviceInterface, 1);
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface, int protocolType) {
        return addConsumer(serviceInterface, null, protocolType);
    }

    public RpcBootstrap addRegister(String baseUrl, String name) {
        registryConfig = new RegistryConfig(baseUrl, name);
        return this;
    }

    public RpcApp build() {
        RpcApp rpcApp = new RpcApp();

        if (providerConfig != null) {
            Provider provider = new Provider(providerConfig.getServiceInterface(), providerConfig.getServiceImpl(), providerConfig.getPort(), providerConfig.getProtocolType());
            rpcApp.setProvider(provider);
            NettyServer nettyServer = new NettyServer(provider.getPort(), provider);
            rpcApp.setServer(nettyServer);
        }

        if (consumerConfigs != null && !consumerConfigs.isEmpty()) {
            List<Consumer> consumers = new ArrayList<>();
            for (ConsumerConfig c : consumerConfigs) {
                Consumer consumer = new Consumer(c.getServiceInterface(), c.getEndpoints(), c.getProtocolType());
                consumers.add(consumer);
            }
            rpcApp.setConsumers(consumers);
            NettyClient nettyClient = new NettyClient();
            rpcApp.setClient(nettyClient);
        }


        if (registryConfig != null) {
            SpiLoader<Registry> loader = new SpiLoader<>(Registry.class);
            Registry registry = loader.get(registryConfig.getName(), registryConfig.getBaseUrl());
            Discovery defaultDiscovery = new DefaultDiscovery(registry, rpcApp.getProvider(), rpcApp.getConsumers(), rpcApp.getClient());
            rpcApp.setDiscovery(defaultDiscovery);
        } else {
            rpcApp.setDiscovery(new NoRegistryDiscovery(rpcApp.getConsumers(), rpcApp.getClient()));
        }

        rpcApp.init();
        return rpcApp;
    }

}
