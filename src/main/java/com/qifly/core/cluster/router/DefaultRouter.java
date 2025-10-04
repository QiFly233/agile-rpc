package com.qifly.core.cluster.router;

import com.qifly.core.transport.TransportClient;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class DefaultRouter implements Router {

    private final TransportClient client;

    public DefaultRouter(TransportClient client) {
        this.client = client;
    }

    @Override
    public List<String> route(String service, List<String> endpoints) {
        List<String> routed = new ArrayList<>();
        // TODO 路由规则
        for (String endpoint : endpoints) {
            Channel channel = client.getChannel(endpoint);
            if (channel != null && channel.isActive()) {
                routed.add(endpoint);
            }
        }
        return routed;
    }
}
