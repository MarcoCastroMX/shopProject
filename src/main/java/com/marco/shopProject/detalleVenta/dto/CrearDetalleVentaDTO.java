package com.marco.shopProject.detalleVenta.dto;

import lombok.Builder;

@Builder
public record CrearDetalleVentaDTO(
        Integer productoId,
        Integer cantidad
) {
}
