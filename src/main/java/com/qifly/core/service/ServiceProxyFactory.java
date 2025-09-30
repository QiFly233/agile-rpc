package com.qifly.core.service;

import com.qifly.core.registry.Discovery;
import com.qifly.core.transport.TransportClient;

import java.lang.reflect.Proxy;

public class ServiceProxyFactory {

    public static Object create(Consumer consumer, TransportClient client, Discovery discovery) {
        ServiceInvocationHandler handler = new ServiceInvocationHandler(consumer, client, discovery);
        return Proxy.newProxyInstance(
                consumer.getItf().getClassLoader(),
                new Class[]{consumer.getItf()},
                handler
        );
    }
}
