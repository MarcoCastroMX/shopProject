package com.marco.shopProject.identity.user.dto;

import com.marco.shopProject.core.tools.enums.RolesEnum;
import lombok.Builder;

import java.util.List;

@Builder
public record CrearUserDTO(
        String nombre,
        String apellido,
        String email,
        String password,
        String telefono,
        List<RolesEnum> roles
) {
}
