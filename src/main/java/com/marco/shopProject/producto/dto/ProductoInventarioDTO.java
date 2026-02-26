package com.marco.shopProject.producto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record ProductoInventarioDTO(
        Long id,

        @NotNull(message = "Debe se tener un nombre")
        String nombre,

        String categoria,

        @NotNull(message = "Debe de tener un precio")
        @PositiveOrZero(message = "Debe tener un precio mayor o igual a 0")
        Double precio,

        @NotNull(message = "Debe de tener un precio")
        @PositiveOrZero(message = "Debe tener una cantidad mayor o igual a 0")
        Integer cantidad
) {
}
