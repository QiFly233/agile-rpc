package com.qifly.core.discovery.registry;

import java.util.List;

public interface RegistryListener {
    void onChange(List<String> endpoints);
}
