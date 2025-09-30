package com.qifly.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> ref) {
        try {
            return MAPPER.readValue(json, ref);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
