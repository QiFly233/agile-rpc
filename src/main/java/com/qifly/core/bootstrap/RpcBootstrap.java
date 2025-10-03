package com.qifly.core.bootstrap;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.config.ConsumerConfig;
import com.qifly.core.bootstrap.config.ProviderConfig;
import com.qifly.core.bootstrap.config.RegistryConfig;
import com.qifly.core.discovery.DefaultDiscovery;
import com.qifly.core.discovery.NoRegistryDiscovery;
import com.qifly.core.discovery.registry.ConsulRegistry;
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
        providerConfig = new ProviderConfig(serviceInterface, serviceImpl, port);
        return this;
    }

    // TODO 修改为直连
    public RpcBootstrap addConsumer(Class<?> serviceInterface, List<String> endpoints) {
        consumerConfigs.add(new ConsumerConfig(serviceInterface, endpoints));
        return this;
    }

    public RpcBootstrap addConsumer(Class<?> serviceInterface) {
        consumerConfigs.add(new ConsumerConfig(serviceInterface));
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
            for (ConsumerConfig c : consumerConfigs) {
                Consumer consumer = new Consumer(c.getServiceInterface(), c.getEndpoints());
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
        } else {
            rpcApp.setDiscovery(new NoRegistryDiscovery(rpcApp.getConsumers(), rpcApp.getClient()));
        }

        rpcApp.init();
        return rpcApp;
    }

}
