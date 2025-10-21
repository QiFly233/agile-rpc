package com.qifly.core.discovery.registry;

import com.qifly.core.exception.RegistryException;
import com.qifly.core.service.Provider;

import java.util.List;

public interface Registry {

    interface SubscribeListener {
        void onChange(List<String> endpoints);
    }

    void register(String baseUrl, Provider provider) throws RegistryException;

    void deregister(String baseUrl, Provider provider) throws RegistryException;

    List<String> discover(String baseUrl, String serviceName) throws RegistryException;

    void subscribe(String baseUrl, String serviceName, SubscribeListener listener);
}
