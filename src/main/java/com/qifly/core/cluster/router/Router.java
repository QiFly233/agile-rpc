package com.qifly.core.cluster.router;

import java.util.List;

public interface Router {

    List<String> route(String service, List<String> endpoints);
}
