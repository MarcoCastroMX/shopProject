package com.marco.shopProject.identity.user.exception;

public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String estado) {
        super(String.format(
                "El estado '%s' no es valido. Valores permitidos: ACTIVO, ELIMINADO",
                estado
        ));
    }
}
