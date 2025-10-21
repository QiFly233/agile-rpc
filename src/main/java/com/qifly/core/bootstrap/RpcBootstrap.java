package com.qifly.core.bootstrap;

import com.qifly.RpcApp;
import com.qifly.core.bootstrap.config.ConsumerConfig;
import com.qifly.core.bootstrap.config.ProviderConfig;
import com.qifly.core.bootstrap.config.RegistryConfig;
import com.qifly.core.discovery.DefaultDiscovery;
import com.qifly.core.discovery.Discovery;
import com.qifly.core.discovery.NoRegistryDiscovery;
import com.qifly.core.discovery.registry.Registry;
import com.qifly.core.discovery.registry.RegistryEntry;
import com.qifly.core.loader.SpiHelper;
import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;
import com.qifly.core.transport.netty.NettyClient;
import com.qifly.core.transport.netty.NettyServer;

import java.util.ArrayList;
import java.util.HashMap;
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
    List<RegistryConfig> registryConfigs = new ArrayList<>();

    public ConsumerConfigBuilder consumer() {
        return new ConsumerConfigBuilder(this);
    }

    public ProviderConfigBuilder provider() {
        return new ProviderConfigBuilder(this);
    }

    public RegistryConfigBuilder registry() {
        return new RegistryConfigBuilder(this);
    }

    private void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    private void addConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfigs.add(consumerConfig);
    }

    private void addRegistryConfig(RegistryConfig registryConfig) {
        registryConfigs.add(registryConfig);
    }

    public RpcApp build() {
        RpcApp rpcApp = new RpcApp();

        if (providerConfig != null) {
            Provider provider = new Provider(providerConfig.getServiceInterface(), providerConfig.getServiceImpl(), providerConfig.getPort(), providerConfig.getProtocolType(), providerConfig.getRegistry());
            rpcApp.setProvider(provider);
            NettyServer nettyServer = new NettyServer(provider.getPort(), provider);
            rpcApp.setServer(nettyServer);
        }

        if (consumerConfigs != null && !consumerConfigs.isEmpty()) {
            List<Consumer> consumers = new ArrayList<>();
            for (ConsumerConfig c : consumerConfigs) {
                Consumer consumer = new Consumer(c.getServiceInterface(), c.getEndpoints(), c.getProtocolType(), c.getRegistry(), c.getRouter(), c.getLoadBalance());
                consumers.add(consumer);
            }
            rpcApp.setConsumers(consumers);
            NettyClient nettyClient = new NettyClient();
            rpcApp.setClient(nettyClient);
        }


        if (registryConfigs != null && !registryConfigs.isEmpty()) {
            HashMap<String, RegistryEntry> registryMap = new HashMap<>();
            for (RegistryConfig c : registryConfigs) {
                Registry registry = SpiHelper.getImpl(Registry.class, c.getRegistry());
                RegistryEntry registryEntry = new RegistryEntry(c.getId(), c.getBaseUrl(), registry);
                registryMap.put(c.getId(), registryEntry);
            }
            Discovery defaultDiscovery = new DefaultDiscovery(rpcApp.getProvider(), rpcApp.getConsumers(), registryMap, rpcApp.getClient());
            rpcApp.setDiscovery(defaultDiscovery);
        } else {
            rpcApp.setDiscovery(new NoRegistryDiscovery(rpcApp.getConsumers(), rpcApp.getClient()));
        }

        rpcApp.init();
        return rpcApp;
    }

    public static class ProviderConfigBuilder {
        private final RpcBootstrap rpcBootstrap;
        private final ProviderConfig config = new ProviderConfig();

        public ProviderConfigBuilder(RpcBootstrap rpcBootstrap) {
            this.rpcBootstrap = rpcBootstrap;
        }

        public ProviderConfigBuilder service(Class<?> serviceInterface, Object serviceImpl) {
            config.setServiceInterface(serviceInterface);
            config.setServiceImpl(serviceImpl);
            return this;
        }

        public ProviderConfigBuilder port(int port) {
            config.setPort(port);
            return this;
        }

        public ProviderConfigBuilder protocolType(int protocolType) {
            config.setProtocolType(protocolType);
            return this;
        }

        public ProviderConfigBuilder registry(String id) {
            config.setRegistry(id);
            return this;
        }

        public RpcBootstrap and() {
            rpcBootstrap.setProviderConfig(config);
            return rpcBootstrap;
        }
    }

    public static class ConsumerConfigBuilder {
        private final RpcBootstrap rpcBootstrap;
        private final ConsumerConfig config = new ConsumerConfig();

        public ConsumerConfigBuilder(RpcBootstrap rpcBootstrap) {
            this.rpcBootstrap = rpcBootstrap;
        }

        public ConsumerConfigBuilder service(Class<?> serviceInterface) {
            config.setServiceInterface(serviceInterface);
            return this;
        }

        public ConsumerConfigBuilder addEndpoint(String endpoint) {
            config.addEndpoint(endpoint);
            return this;
        }

        public ConsumerConfigBuilder protocolType(int protocolType) {
            config.setProtocolType(protocolType);
            return this;
        }

        public ConsumerConfigBuilder router(String router) {
            config.setRouter(router);
            return this;
        }

        public ConsumerConfigBuilder loadBalance(String loadBalance) {
            config.setLoadBalance(loadBalance);
            return this;
        }

        public ConsumerConfigBuilder registry(String id) {
            config.setRegistry(id);
            return this;
        }

        public RpcBootstrap and() {
            rpcBootstrap.addConsumerConfig(config);
            return rpcBootstrap;
        }
    }

    public static class RegistryConfigBuilder {
        private final RpcBootstrap rpcBootstrap;
        private final RegistryConfig config = new RegistryConfig();

        public RegistryConfigBuilder(RpcBootstrap rpcBootstrap) {
            this.rpcBootstrap = rpcBootstrap;
        }

        public RegistryConfigBuilder id(String id) {
            config.setId(id);
            return this;
        }

        public RegistryConfigBuilder baseUrl(String baseUrl) {
            config.setBaseUrl(baseUrl);
            return this;
        }

        public RegistryConfigBuilder registry(String registry) {
            config.setRegistry(registry);
            return this;
        }

        public RpcBootstrap and() {
            rpcBootstrap.addRegistryConfig(config);
            return rpcBootstrap;
        }
    }
}
