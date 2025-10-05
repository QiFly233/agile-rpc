package com.qifly.core.protocol.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class FrameCodec extends ByteToMessageCodec<RpcFrame> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcFrame rpcFrame, ByteBuf byteBuf) throws Exception {
        ByteBuf body = rpcFrame.getBody() == null ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(rpcFrame.getBody());
        byteBuf.writeShort(RpcFrame.MAGIC);
        byteBuf.writeByte(rpcFrame.getFlags());
        byteBuf.writeByte(rpcFrame.getStatus());
        byteBuf.writeInt(body.readableBytes());
        byteBuf.writeLong(rpcFrame.getRequestId());
        if (body.readableBytes() > 0) byteBuf.writeBytes(body);
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

        byte[] body = new byte[length];
        if (length > 0) {
            byteBuf.readBytes(body);
        }

        list.add(new RpcFrame(magic, flags, status, length, reqId, body));
    }
}
