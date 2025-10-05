package com.qifly.core.protocol.frame;

public class RpcFrameStatus {

    /**
     * 成功
     */
    public static final byte SUCCESS = 0;

    /**
     * 不支持的包体协议
     */
    public static final byte UNSUPPORTED_BODY_PROTOCOL = 21;

    /**
     * 不一致的包体协议
     */
    public static final byte INCONSISTENT_BODY_PROTOCOL = 21;
}
