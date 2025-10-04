package com.qifly.core.cluster;

import com.qifly.core.cluster.loadbalance.LoadBalance;
import com.qifly.core.cluster.router.Router;
import com.qifly.core.discovery.Discovery;

import java.util.List;

public class DefaultCluster implements Cluster {

    private final Discovery discovery;
    private final LoadBalance loadBalance;
    private final Router router;

    public DefaultCluster(Discovery discovery, LoadBalance loadBalance, Router router) {
        this.discovery = discovery;
        this.loadBalance = loadBalance;
        this.router = router;
    }

    public String getEndpoint(String serviceName) {
        List<String> endpoints = discovery.discover(serviceName);
        List<String> routed = router.route(serviceName, endpoints);
        return loadBalance.select(serviceName, routed);
    }
}
