package com.marco.shopProject.identity.auth.exception;

public class RefreshTokenInvalidoException extends RuntimeException {

    public RefreshTokenInvalidoException(String message) {
        super(message);
    }

    public RefreshTokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
