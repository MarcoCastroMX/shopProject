package com.marco.shopProject.catalog.sucursal.exception;

public class SucursalNoEncontradaException extends RuntimeException {
    public SucursalNoEncontradaException(String message) {
        super(message);
    }
    public SucursalNoEncontradaException(Long id){
        super(String.valueOf(id));
    }
}
