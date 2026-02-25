package com.marco.shopProject.venta.exception;

public class VentaNoEncontradaException extends RuntimeException {
    public VentaNoEncontradaException(String message) {
        super(message);
    }
    public VentaNoEncontradaException(Long id){
        super(String.valueOf(id));
    }
}
