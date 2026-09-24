package com.marco.shopProject.sales.venta.exception;

import java.time.LocalDateTime;

public class FechaInvalidaException extends RuntimeException {

    public FechaInvalidaException(LocalDateTime fecha) {
        super(String.format(
                "La fecha '%s' no es valida. No se permiten fechas futuras",
                fecha
        ));
    }
}
