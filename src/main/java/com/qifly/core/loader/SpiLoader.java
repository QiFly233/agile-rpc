package com.qifly.core.loader;

import com.qifly.core.exception.RpcException;

import java.lang.reflect.Constructor;
import java.util.*;

public class SpiLoader<T> {
    private final Class<T> intf;
    private final Map<String, Class<? extends T>> classMap = new HashMap<>();
    private final Map<String, T> implMap = new HashMap<>();

    // TODO 可能优化为不依赖SPI，手动解析
    public SpiLoader(Class<T> intf) {
        this.intf = intf;
        ServiceLoader<T> loader = ServiceLoader.load(intf);
        for (T impl : loader) {
            if (impl == null) continue;
            Class<?> implClass = impl.getClass();
            String key = implClass.getSimpleName();
            classMap.put(key, implClass.asSubclass(intf));
            implMap.put(key, impl);
        }
    }

    public T get(String className) {
        return implMap.get(className);
    }

    public T get(String name, Object... args) {
        Class<? extends T> impl = classMap.get(name);
        if (impl == null) {
            throw new RpcException("no such implementation of " + name);
        }
        Constructor<?> constructor = getConstructor(impl, args);
        if (constructor == null) {
            throw new RpcException("no public constructor of " + impl.getName());
        }
        try {
            Object o = constructor.newInstance(args);
            return intf.cast(o);
        } catch (Exception e) {
            throw new RpcException("create " + impl.getName() + " failed", e);
        }
    }

    private Constructor<?> getConstructor(Class<? extends T> impl, Object... args) {
        List<Constructor<?>> candidates = new ArrayList<>();
        Constructor<?>[] constructors = impl.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != args.length) {
                continue;
            }

            boolean match = true;
            for (int i = 0; i < paramTypes.length; i++) {
                Object arg = args[i];
                Class<?> paramType = paramTypes[i];
                if (arg == null) {
                    if (paramType.isPrimitive()) {
                        match = false;
                        break;
                    }
                } else {
                    Class<?> expected = wrapPrimitive(paramType);
                    if (!expected.equals(arg.getClass())) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                candidates.add(constructor);
            }
        }
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    private static Class<?> wrapPrimitive(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == boolean.class) return Boolean.class;
        if (c == byte.class) return Byte.class;
        if (c == char.class) return Character.class;
        if (c == short.class) return Short.class;
        if (c == float.class) return Float.class;
        if (c == double.class) return Double.class;
        return c;
    }
}
