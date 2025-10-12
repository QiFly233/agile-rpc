package com.qifly.core.exception;

public class RpcBodyHandlerException extends RpcException{
    public RpcBodyHandlerException(String message) {
        super(message);
    }

    public RpcBodyHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
