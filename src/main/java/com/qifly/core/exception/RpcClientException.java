package com.qifly.core.exception;

public class RpcClientException extends RpcException{

    public RpcClientException(String message) {
        super(message);
    }

    public RpcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
