package com.qifly.core.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class SpiLoader<T> {

    private final Class<T> intf;
    private final Map<String, T> implMap = new HashMap<>();

    public SpiLoader(Class<T> intf) {
        this.intf = intf;
        ServiceLoader<T> loader = ServiceLoader.load(intf);
        for (T impl : loader) {
            if (impl == null) continue;
            Class<?> implClass = impl.getClass();
            String key = implClass.getSimpleName();
            implMap.put(key, impl);
        }
    }

    public T get(String name) {
        return implMap.get(name);
    }
}
