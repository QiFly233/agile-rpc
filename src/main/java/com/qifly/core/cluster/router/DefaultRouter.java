package com.qifly.core.cluster.router;

import java.util.List;

public class DefaultRouter implements Router {

    @Override
    public List<String> route(String service, List<String> endpoints) {
        return endpoints;
    }
}
