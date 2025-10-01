package com.qifly.core.discovery;

public interface Discovery {

    void start();

    void register();

    void deregister();

    String discover(String serviceName);
}
