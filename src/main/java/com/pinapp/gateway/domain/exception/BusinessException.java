package com.pinapp.gateway.domain.exception;

/**
 * Base exception for business-related errors in the domain layer.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
