package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record SucursalDTO(
        Long id,
        String nombre,
        String direccion
) {
}
