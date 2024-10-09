package com.example.currencies.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException() {
        super("service unavailable");
    }
}
