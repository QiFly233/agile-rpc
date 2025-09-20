package com.qifly.core.protocol.frame;

import io.netty.buffer.ByteBuf;

/**
 * 自定义协议格式
 */
public class RpcFrame {
    public static final short MAGIC = (short) 0xBB01;
    public static final int HEADER_LEN = 16;

    private final short magic; // 魔数（16bit），唯一标识（8bit） + 版本号（8bit）
    private final byte flags; // 预留（2bit） + 是否心跳（1bit） + 请求/响应（1bit） + 协议（4bit）
    private final byte status; // 状态（8bit）
    private final int length; // 负载长度（32bit）
    private final long requestId; // 请求Id（64bit）
    private final ByteBuf body;

    public RpcFrame(short magic, byte flags, byte status, int length, long requestId, ByteBuf body) {
        this.magic = magic;
        this.flags = flags;
        this.status = status;
        this.length = length;
        this.requestId = requestId;
        this.body = body;
    }

    public static RpcFrame request(int protoId, boolean heartbeat, long requestId, ByteBuf body) {
        byte f = Flag.build(protoId, true, heartbeat);
        return new RpcFrame(MAGIC, f, (byte) 0, body.readableBytes(), requestId, body);
    }

    public static RpcFrame response(RpcFrame req, byte status, ByteBuf body) {
        byte f = Flag.asResponse(req.getFlags());
        return new RpcFrame(MAGIC, f, status, body.readableBytes(), req.getRequestId(), body);
    }


    public short getMagic() {
        return magic;
    }

    public byte getFlags() {
        return flags;
    }

    public byte getStatus() {
        return status;
    }

    public int getLength() {
        return length;
    }

    public long getRequestId() {
        return requestId;
    }

    public ByteBuf getBody() {
        return body;
    }

    public boolean isRequest() {
        return Flag.isRequest(flags);
    }

    public boolean isHeartbeat() {
        return Flag.isHeartbeat(flags);
    }

    public int getProtocolType() {
        return Flag.protocolType(flags);
    }

    private static class Flag {
        private static final int PROTO_MASK = 0b0000_1111;
        private static final int REQ_BIT    = 1 << 4;
        private static final int HB_BIT     = 1 << 5;

        private Flag() {}

        public static int protocolType(byte flags) {
            return flags & PROTO_MASK;
        }

        public static boolean isRequest(byte flags) {
            return (flags & REQ_BIT) != 0;
        }

        public static boolean isHeartbeat(byte flags) {
            return (flags & HB_BIT) != 0;
        }

        public static byte build(int protoId, boolean request, boolean heartbeat) {
            int f = (protoId & PROTO_MASK)
                    | (request ? REQ_BIT : 0)
                    | (heartbeat ? HB_BIT : 0);
            return (byte) (f & 0xFF);
        }

        public static byte asResponse(byte flags) {
            return (byte) (flags & ~REQ_BIT);
        }
    }
}
