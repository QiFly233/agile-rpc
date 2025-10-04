package com.qifly.core.exception;

public class DiscoveryException extends RpcException{

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
