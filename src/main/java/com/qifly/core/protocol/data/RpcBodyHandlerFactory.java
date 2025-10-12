package com.qifly.core.protocol.data;

import com.qifly.core.exception.RpcBodyHandlerException;
import com.qifly.core.protocol.data.protobuf.ProtobufBodyHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcBodyHandlerFactory {

    private static final ConcurrentMap<Integer, RpcBodyHandler> map = new ConcurrentHashMap<>();

    public static RpcBodyHandler getHandler(int protocolType) {
        return map.computeIfAbsent(protocolType, key -> {
            if (key == 1) {
                return new ProtobufBodyHandler();
            } else {
                throw new RpcBodyHandlerException("unknown protocol");
            }
        });
    }
}
