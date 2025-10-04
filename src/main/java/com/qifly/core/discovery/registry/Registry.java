package com.qifly.core.discovery.registry;

import com.qifly.core.exception.RegistryException;
import com.qifly.core.service.Provider;

import java.util.List;

public interface Registry {

    interface SubscribeListener {
        void onChange(List<String> endpoints);
    }

    void register(Provider provider) throws RegistryException;

    void deregister(Provider provider) throws RegistryException;

    List<String> discover(String serviceName) throws RegistryException;

    void subscribe(String serviceName, SubscribeListener listener);
}
