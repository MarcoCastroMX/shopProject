package com.marco.shopProject.identity.user.exception;

public class UsuarioEliminadoException extends RuntimeException {

    public UsuarioEliminadoException(String message) {
        super(message);
    }

    public UsuarioEliminadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
