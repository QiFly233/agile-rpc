package com.qifly.core.protocol.frame;

import com.qifly.core.protocol.frame.meta.RpcMetaData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class FrameCodec extends ByteToMessageCodec<RpcFrame> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcFrame rpcFrame, ByteBuf byteBuf) throws Exception {
        RpcMetaData rpcMetaData = rpcFrame.getRpcMetaData();
        int metaLen = (rpcMetaData == null) ? 0 : rpcMetaData.getSerializedSize();
        byte[] rpcBodyBytes = rpcFrame.getRpcBody() == null ? new byte[0] : rpcFrame.getRpcBody();
        int length = 4 + metaLen + rpcBodyBytes.length;

        byteBuf.writeShort(RpcFrame.MAGIC);
        byteBuf.writeByte(rpcFrame.getFlags());
        byteBuf.writeByte(rpcFrame.getStatus());
        byteBuf.writeInt(length);
        byteBuf.writeLong(rpcFrame.getRequestId());

        // body (int + meta + rpcBody)
        byteBuf.writeInt(metaLen);
        if (metaLen > 0) {
            rpcMetaData.writeTo(new ByteBufOutputStream(byteBuf));
        }
        byteBuf.writeBytes(rpcBodyBytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < RpcFrame.HEADER_LEN) {
            return;
        }

        short magic = byteBuf.readShort();
        if (magic != RpcFrame.MAGIC) {
            ctx.close();
            return;
        }

        byte flags = byteBuf.readByte();
        byte status = byteBuf.readByte();
        int length = byteBuf.readInt();
        long reqId = byteBuf.readLong();

        if (length < 4) {
            ctx.close();
            return;
        }

        int rpcMetaDataLength = byteBuf.readInt();
        if (rpcMetaDataLength < 0) {
            ctx.close();
            return;
        }

        byte[] rpcMetaDataBytes = new byte[rpcMetaDataLength];
        byteBuf.readBytes(rpcMetaDataBytes);
        RpcMetaData rpcMetaData = RpcMetaData.parseFrom(rpcMetaDataBytes);

        int rpcBodyLength = length - 4 - rpcMetaDataLength;
        if (rpcBodyLength < 0) {
            ctx.close();
            return;
        }
        byte[] rpcBody = new byte[rpcBodyLength];
        byteBuf.readBytes(rpcBody);

        list.add(new RpcFrame(magic, flags, status, length, reqId, rpcMetaData, rpcBody));
    }
}
