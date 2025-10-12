package com.qifly.core.protocol.data;

import com.qifly.core.service.Provider;

public interface RpcBodyHandler {

    byte[] toReq(int rpcId, Object o);

    Object toResp(byte[] bytes, Class<?> respType);

    byte[] respond(Provider provider, byte[] bytes);
}
