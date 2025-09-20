package com.qifly;

import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;
import com.qifly.core.service.ServiceProxyFactory;
import com.qifly.core.transport.netty.NettyClient;
import com.qifly.core.transport.netty.NettyServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC应用
 */
public class RpcApp {

    /**
     * 生产者
     */
    private Provider provider;

    /**
     * 消费者
     */
    private List<Consumer> consumers;

    /**
     * 消费者代理类
     */
    private final Map<String, Object> consumerProxy = new HashMap<>();

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConsumer(String consumerName) {
        return (T) consumerProxy.get(consumerName);
    }

    public void init() {
        if (provider != null) {
            try {
                NettyServer nettyServer = new NettyServer(provider.getPort(), provider);
                nettyServer.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!consumers.isEmpty()) {
            NettyClient nettyClient = new NettyClient(consumers);
            for (Consumer consumer : consumers) {
                // TODO 从注册中心获取地址
                try {
                    nettyClient.connect("127.0.0.1", consumer.getPort());
                    consumerProxy.put(consumer.getItf().getSimpleName(), ServiceProxyFactory.create(consumer, nettyClient));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
