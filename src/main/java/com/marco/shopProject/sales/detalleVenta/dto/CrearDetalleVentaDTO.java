package com.marco.shopProject.sales.detalleVenta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CrearDetalleVentaDTO(
        @NotNull(message = "Debe de tener un ID")
        Integer productoId,

        @NotNull(message = "Debe de tener una cantidad")
        @Positive(message = "Debe tener una cantidad mayor a 0")
        Integer cantidad
) {
}
