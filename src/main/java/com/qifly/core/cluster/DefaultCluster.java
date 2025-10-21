package com.qifly.core.cluster;

import com.qifly.core.cluster.loadbalance.LoadBalance;
import com.qifly.core.cluster.router.Router;
import com.qifly.core.discovery.Discovery;

import java.util.List;
import java.util.Map;

public class DefaultCluster implements Cluster {

    private final Discovery discovery;
    private final Map<String, LoadBalance> loadBalanceMap;
    private final Map<String, Router> routerMap;

    public DefaultCluster(Discovery discovery, Map<String, Router> routerMap, Map<String, LoadBalance> loadBalanceMap) {
        this.discovery = discovery;
        this.routerMap = routerMap;
        this.loadBalanceMap = loadBalanceMap;
    }

    public String getEndpoint(String serviceName) {
        List<String> endpoints = discovery.discover(serviceName);
        List<String> routed = routerMap.get(serviceName).route(serviceName, endpoints);
        return loadBalanceMap.get(serviceName).select(serviceName, routed);
    }
}
