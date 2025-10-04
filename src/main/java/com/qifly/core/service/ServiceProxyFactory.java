package com.qifly.core.service;

import com.qifly.core.cluster.Cluster;
import com.qifly.core.transport.TransportClient;

import java.lang.reflect.Proxy;

public class ServiceProxyFactory {

    public static Object create(Consumer consumer, TransportClient client, Cluster cluster) {
        ServiceInvocationHandler handler = new ServiceInvocationHandler(consumer, client, cluster);
        return Proxy.newProxyInstance(
                consumer.getItf().getClassLoader(),
                new Class[]{consumer.getItf()},
                handler
        );
    }
}
