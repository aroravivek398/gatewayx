package com.gatewayx.exception;

public class ApiKeyLimitExceededException extends RuntimeException {
    public ApiKeyLimitExceededException(String message){
        super(message);
    }
}
