package com.qifly.core.cluster.loadbalance;

import java.util.List;

public interface LoadBalance {

    String select(String serviceName, List<String> endpoints);
}
