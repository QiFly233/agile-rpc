package com.qifly.core.cluster.loadbalance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {

    private final Map<String, AtomicInteger> map = new ConcurrentHashMap<>();

    @Override
    public String select(String serviceName, List<String> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return null;
        }
        int index = getNextIndex(serviceName, endpoints);
        return endpoints.get(index);
    }

    private int getNextIndex(String serviceName, List<String> endpoints) {
        AtomicInteger atomicInteger = map.computeIfAbsent(serviceName, k -> new AtomicInteger());
        return atomicInteger.getAndIncrement() % endpoints.size();
    }
}
