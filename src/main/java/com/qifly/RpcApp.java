package com.qifly;

import com.qifly.core.cluster.Cluster;
import com.qifly.core.cluster.DefaultCluster;
import com.qifly.core.cluster.loadbalance.LoadBalance;
import com.qifly.core.cluster.router.Router;
import com.qifly.core.discovery.Discovery;
import com.qifly.core.loader.SpiHelper;
import com.qifly.core.service.Consumer;
import com.qifly.core.service.Provider;
import com.qifly.core.service.ServiceProxyFactory;
import com.qifly.core.transport.TransportClient;
import com.qifly.core.transport.TransportServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC应用
 */
public class RpcApp implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(RpcApp.class);

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

    /**
     * 服务发现者
     */
    private Discovery discovery;

    /**
     * 服务端
     */
    private TransportServer server;

    /**
     * 客户端
     */
    private TransportClient client;

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

    public Map<String, Object> getConsumerProxy() {
        return consumerProxy;
    }

    public Discovery getDiscovery() {
        return discovery;
    }

    public void setDiscovery(Discovery discovery) {
        this.discovery = discovery;
    }

    public void setServer(TransportServer server) {
        this.server = server;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }

    public TransportServer getServer() {
        return server;
    }

    public TransportClient getClient() {
        return client;
    }

    public void init() {
        if (consumers != null && !consumers.isEmpty()) {
            Map<String, Router> routerMap = new HashMap<>();
            Map<String, LoadBalance> loadBalanceMap = new HashMap<>();
            Cluster cluster = new DefaultCluster(discovery, routerMap, loadBalanceMap);
            for (Consumer consumer : consumers) {
                Router router = SpiHelper.getImpl(Router.class, consumer.getRouter());
                routerMap.put(consumer.getServiceName(), router);
                LoadBalance loadbalance = SpiHelper.getImpl(LoadBalance.class, consumer.getLoadBalance());
                loadBalanceMap.put(consumer.getServiceName(), loadbalance);
            }
            for (Consumer consumer : consumers) {
                consumerProxy.put(consumer.getItf().getSimpleName(), ServiceProxyFactory.create(consumer, client, cluster));
            }
        }
    }

    public void start() {
        if (discovery != null) {
            discovery.start();
        }

        if (provider != null) {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (discovery != null) {
            discovery.register();
        }

    }

    @Override
    public void close() {
        if (discovery != null) {
            discovery.close();
        }
        if (server != null) {
            server.close();
        }
        if (discovery != null) {
            discovery.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
