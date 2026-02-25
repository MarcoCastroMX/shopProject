package com.marco.shopProject.sucursal.dto;

import lombok.Builder;

@Builder
public record SucursalDTO(
        Long id,
        String nombre,
        String direccion
) {
}
