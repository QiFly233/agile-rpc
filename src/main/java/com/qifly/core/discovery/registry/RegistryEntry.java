package com.qifly.core.discovery.registry;

public class RegistryEntry {

    private final String id;
    private final String baseUrl;
    private final Registry registry;

    public RegistryEntry(String id, String baseUrl, Registry registry) {
        this.id = id;
        this.baseUrl = baseUrl;
        this.registry = registry;
    }

    public String getId() {
        return id;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Registry getRegistry() {
        return registry;
    }
}
