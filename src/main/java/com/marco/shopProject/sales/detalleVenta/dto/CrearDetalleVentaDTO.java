package com.marco.shopProject.sales.detalleVenta.dto;

import lombok.Builder;

@Builder
public record CrearDetalleVentaDTO(
        Integer productoId,
        Integer cantidad
) {
}
