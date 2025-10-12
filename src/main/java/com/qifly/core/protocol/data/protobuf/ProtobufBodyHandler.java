package com.qifly.core.protocol.data.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qifly.core.exception.RpcBodyHandlerException;
import com.qifly.core.protocol.data.RpcBodyHandler;
import com.qifly.core.service.Provider;
import com.qifly.core.service.RpcMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtobufBodyHandler implements RpcBodyHandler {

    @Override
    public byte[] toReq(int rpcId, Object o) {
         return RpcBody.newBuilder()
                .setRpcId(rpcId)
                .setData(Any.pack((Message) o))
                .build()
                .toByteArray();
    }

    @Override
    public Object toResp(byte[] bytes, Class<?> respType) {
        try {
            RpcBody resBody = RpcBody.parseFrom(bytes);
            Any any = resBody.getData();
            return any.unpack((Class<? extends Message>) respType);
        } catch (InvalidProtocolBufferException e) {
            throw new RpcBodyHandlerException("protobuf parse error", e);
        }
    }

    @Override
    public byte[] respond(Provider provider, byte[] bytes) {
        RpcBody reqBody = null;
        try {
            reqBody = RpcBody.parseFrom(bytes);
            Any any = reqBody.getData();
            RpcMethod rpcMethod = provider.getRpcMethod(reqBody.getRpcId());
            Message req = any.unpack((Class<? extends Message>)rpcMethod.getReqType());
            Method method = rpcMethod.getMethod();
            Message resp = (Message) method.invoke(provider.getImpl(), req);
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_SUCCESS)
                    .setData(Any.pack(resp))
                    .build()
                    .toByteArray();
        } catch (InvalidProtocolBufferException e) {
            return RpcBody.newBuilder()
                    .setStatusCode(RpcStatusCode.RPC_METHOD_REQUEST_ERROR)
                    .build()
                    .toByteArray();
        } catch (InvocationTargetException | IllegalAccessException e) {
            return RpcBody.newBuilder()
                    .setRpcId(reqBody.getRpcId())
                    .setStatusCode(RpcStatusCode.RPC_METHOD_NOT_FOUND_ERROR)
                    .build()
                    .toByteArray();
        }
    }
}
