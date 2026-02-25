package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record CrearDetalleVentaDTO(
        Integer productoId,
        Integer cantidad
) {
}
