package com.marco.shopProject.sales.venta.exception;

public class VentaNoEncontradaException extends RuntimeException {
    public VentaNoEncontradaException(String message) {
        super(message);
    }
    public VentaNoEncontradaException(Long id){
        super(String.valueOf(id));
    }
}
