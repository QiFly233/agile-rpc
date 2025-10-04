package com.qifly.core.exception;

public class RegistryException extends RpcException{

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
