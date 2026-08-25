package com.gatewayx.exception;

public class DeveloperNotFoundException extends RuntimeException {
    public DeveloperNotFoundException(String message){
        super((message));
    }

}
