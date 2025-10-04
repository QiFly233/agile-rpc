package com.qifly.core.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.qifly.core.protocol.data.RpcBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RpcBodyUtil {

    public static RpcBody parseFrom(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        return RpcBody.parseFrom(bytes);
    }

    public static ByteBuf parseFrom(RpcBody body) {
        return Unpooled.wrappedBuffer(body.toByteArray());
    }

}
