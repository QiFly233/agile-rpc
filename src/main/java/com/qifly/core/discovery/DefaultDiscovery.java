package com.qifly.core.discovery;

import com.qifly.core.discovery.registry.Registry;
import com.qifly.core.exception.RegistryException;
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

    /**
     * 仅缓存注册中心的地址
     * 不表示是否已经与服务地址建立连接
     */
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
        // TODO 等待启动时connect完成后再结束
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
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
    public List<String> discover(String serviceName) {
        List<String> endpoints = endpointMap.get(serviceName);
        if (endpoints == null) {
            return Collections.emptyList();
        }
        return endpoints;
    }

    private void notifyServiceChange(String serviceName, List<String> endpoints) {
        List<String> oldEndpoints = endpointMap.getOrDefault(serviceName, Collections.emptyList());
        endpointMap.put(serviceName, endpoints);
        if (client != null) {
            // 上线节点
            for (String endpoint : endpoints) {
                if (!oldEndpoints.contains(endpoint)) {
                    client.connect(endpoint);
                }
            }
            // 下线节点
            for (String old : oldEndpoints) {
                if (!endpoints.contains(old)) {
                    client.disconnect(old);
                }
            }
        }
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
