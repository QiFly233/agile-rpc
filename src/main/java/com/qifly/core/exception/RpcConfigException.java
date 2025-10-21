package com.qifly.core.exception;

public class RpcConfigException extends RpcException{
    public RpcConfigException(String message) {
        super(message);
    }

    public RpcConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
