package com.marco.shopProject.catalog.sucursal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SucursalDTO(
        Long id,

        @NotNull(message = "Las sucursales deben de tener un nombre")
        String nombre,
        String direccion
) {
}
