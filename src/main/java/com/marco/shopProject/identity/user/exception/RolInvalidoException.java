package com.marco.shopProject.identity.user.exception;

public class RolInvalidoException extends RuntimeException {

    public RolInvalidoException(String rol) {
        super(String.format(
                "El rol '%s' no es valido. Valores permitidos: ROLE_ADMIN, ROLE_USER, ROLE_MANAGER",
                rol
        ));
    }
}
