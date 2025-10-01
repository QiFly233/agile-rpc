package com.qifly.core.discovery;

import com.qifly.core.service.Consumer;
import com.qifly.core.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoRegistryDiscovery implements Discovery {

    Logger logger = LoggerFactory.getLogger(NoRegistryDiscovery.class);

    private final List<Consumer> consumers;

    private final TransportClient client;

    private final ConcurrentMap<String, List<String>> endpointMap = new ConcurrentHashMap<>();

    public NoRegistryDiscovery(List<Consumer> consumers, TransportClient client) {
        this.consumers = consumers;
        this.client = client;
    }

    @Override
    public void start() {
        if (consumers == null || consumers.isEmpty()) {
            return;
        }

        for (Consumer consumer : consumers) {
            List<String> endpoints = consumer.getEndpoints();
            if (consumer.getEndpoints() == null || consumer.getEndpoints().isEmpty()) {
                continue;
            }
            for (String endpoint : endpoints) {
                try {
                    client.connect(endpoint);
                } catch (InterruptedException e) {
                    logger.warn("client connect {} error", endpoint, e);
                }
            }
            endpointMap.put(consumer.getServiceName(), endpoints);
        }
    }

    @Override
    public void register() {

    }

    @Override
    public void deregister() {

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
}
