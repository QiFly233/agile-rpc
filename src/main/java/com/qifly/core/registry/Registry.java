package com.qifly.core.registry;

import com.qifly.core.service.Provider;

import java.util.List;

public interface Registry {

    void register(Provider provider) throws RegistryException;

    void deregister(Provider provider) throws RegistryException;

    List<String> discover(String serviceName) throws RegistryException;

    void subscribe(String serviceName, RegistryListener listener);
}
