package com.qifly.core.discovery.registry;

public class RegistryException extends Exception{

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
