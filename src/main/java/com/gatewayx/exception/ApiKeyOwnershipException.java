package com.gatewayx.exception;

public class ApiKeyOwnershipException extends RuntimeException {
    public ApiKeyOwnershipException(String message){
        super(message);
    }
}
