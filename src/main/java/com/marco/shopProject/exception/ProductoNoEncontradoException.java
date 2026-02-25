package com.marco.shopProject.exception;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(String message) {
        super(message);
    }
    public ProductoNoEncontradoException(int id){
        super(String.valueOf(id));
    }
}
