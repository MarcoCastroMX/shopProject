package com.marco.shopProject.user.dto;

import com.marco.shopProject.enums.RolesEnum;
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
