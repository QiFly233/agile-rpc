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
            endpointMap.put(consumer.getServiceName(), endpoints);
            if (client != null) {
                for (String endpoint : endpoints) {
                    while(!client.connectSync(endpoint));
                }
            }
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void register() {

    }

    @Override
    public void deregister() {

    }

    @Override
    public List<String> discover(String serviceName) {
        return endpointMap.get(serviceName);
    }
}
