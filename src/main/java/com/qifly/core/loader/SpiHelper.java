package com.qifly.core.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpiHelper {

    private static final Map<Class<?>, SpiLoader<?>> loaderMap = new ConcurrentHashMap<>();

    public static <T> SpiLoader<T> getSpiLoader(Class<T> intf) {
        return (SpiLoader<T>) loaderMap.computeIfAbsent(intf, SpiLoader::new);
    }

    public static <T> T getImpl(Class<T> intf, String name) {
        SpiLoader<T> spiLoader = getSpiLoader(intf);
        if (spiLoader == null) {
            return null;
        }
        return spiLoader.get(name);
    }
}
