package com.qifly.core.discovery;

import java.util.List;

public interface Discovery {

    void start();

    void close();

    void register();

    void deregister();

    List<String> discover(String serviceName);
}
