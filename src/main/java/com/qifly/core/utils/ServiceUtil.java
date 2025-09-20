package com.qifly.core.utils;

import java.lang.reflect.Method;

/**
 * 获取服务信息工具类
 */
public class ServiceUtil {

    public static int getRpcId(Class<?> itf, Method method) {
        try {
            return itf.getField(method.getName() + "RpcId").getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return 0;
    }
}
