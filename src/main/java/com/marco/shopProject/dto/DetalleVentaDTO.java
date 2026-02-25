package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record DetalleVentaDTO(
        Integer productoId,
        Integer cantidad,
        Double precioUnitario,
        MostrarProductoDTO productoDTO,
        Double subTotal
) {
}
