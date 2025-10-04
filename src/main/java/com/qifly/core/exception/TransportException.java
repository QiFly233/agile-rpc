package com.qifly.core.exception;

public class TransportException extends RpcException{

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
