package com.qifly.core.registry;

import java.util.List;

public interface RegistryListener {
    void onChange(List<String> endpoints);
}
