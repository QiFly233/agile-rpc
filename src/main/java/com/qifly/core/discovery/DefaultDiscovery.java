package com.qifly.core.discovery;

import com.qifly.core.discovery.registry.Registry;
import com.qifly.core.discovery.registry.RegistryException;
import com.qifly.core.retry.RetryExecutor;
import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;
import com.qifly.core.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 计划 快照缓存+过期返回并刷新+定时后台刷新
 */
public class DefaultDiscovery implements Discovery {

    Logger logger = LoggerFactory.getLogger(DefaultDiscovery.class);

    private final Registry registry;

    private final Provider provider;

    private final List<Consumer> consumers;

    // 已发现的服务端点
    private final ConcurrentMap<String, List<String>> endpointMap = new ConcurrentHashMap<>();

    // TODO 统一线程管理
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    private final TransportClient client;

    public DefaultDiscovery(Registry registry, Provider provider, List<Consumer> consumers, TransportClient client) {
        this.registry = registry;
        this.provider = provider;
        this.consumers = consumers;
        this.client = client;
    }

    @Override
    public void start() {
        register();
        if (consumers != null && !consumers.isEmpty()) {
            for (Consumer consumer : consumers) {
                if (consumer.getEndpoints() != null && !consumer.getEndpoints().isEmpty()) {
                    notifyServiceChange(consumer.getServiceName(), consumer.getEndpoints());
                }
                else {
                    subscribe(consumer.getServiceName());
                    // 首轮更新必须完成
                    while (endpointMap.get(consumer.getServiceName()) == null) ;
                }
            }
        }
    }

    @Override
    public void register() {
        if (provider == null) {
            return;
        }
        // 注册服务必须成功
        while (true) {
            try {
                registry.register(provider);
                logger.info("register service:{} success", provider.getServiceName());
                return;
            } catch (RegistryException e) {
                logger.error("register service:{} failed and retry", provider.getServiceName(), e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }
    }

    @Override
    public void deregister() {
        if (provider == null) {
            return;
        }
        try {
            registry.deregister(provider);
            logger.info("deregister service:{} success", provider.getServiceName());
        } catch (Exception e) {
            RetryExecutor.executeAsync("registry-deregister", () -> registry.deregister(provider));
        }
    }

    @Override
    public String discover(String serviceName) {
        List<String> endpoints = endpointMap.get(serviceName);
        if (endpoints == null || endpoints.isEmpty()) {
            return null;
        }
        // TODO 策略 channel
        for (String endpoint : endpoints) {
            if (client.getChannel(endpoint) != null) {
                return endpoint;
            }
        }
        return null;
    }

    private void notifyServiceChange(String serviceName, List<String> endpoints) {
        List<String> oldEndpoints = endpointMap.getOrDefault(serviceName, Collections.emptyList());
        if (client != null) {
            // 下线节点
            for (String old : oldEndpoints) {
                if (!endpoints.contains(old)) {
                    try {
                        client.disconnect(old);
                    } catch (InterruptedException e) {
                        logger.error("client disconnect {} error", old, e);
                    }
                }
            }
            // 新增节点
            for (String endpoint : endpoints) {
                if (!oldEndpoints.contains(endpoint)) {
                    try {
                        client.connect(endpoint);
                    } catch (InterruptedException e) {
                        logger.warn("client connect {} error", endpoint, e);
                    }
                }
            }
        }
        /**
         * 只缓存由注册中心发现的服务端点，与服务连接/断连无关
         * 可能存在短暂的与client中的channelMap存在不一致，但无关紧要，选择client.channel时只需要判断是否在channelMap即可
         */
        endpointMap.put(serviceName, endpoints);
    }

    private void subscribe(String serviceName) {
        asyncExecutor.submit(() -> {
            registry.subscribe(serviceName, endpoints -> {
                logger.info("subscribe service change to {}", endpoints);
                notifyServiceChange(serviceName, endpoints);
            });
        });
    }

}
