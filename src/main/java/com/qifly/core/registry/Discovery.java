package com.qifly.core.registry;

public interface Discovery {

    void start();

    void register();

    void deregister();

    String discover(String serviceName);
}
