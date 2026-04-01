package com.marco.shopProject.sales.detalleVenta.dto;

import com.marco.shopProject.catalog.producto.dto.MostrarProductoDTO;
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
