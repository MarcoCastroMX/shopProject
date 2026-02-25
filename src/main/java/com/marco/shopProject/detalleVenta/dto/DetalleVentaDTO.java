package com.marco.shopProject.detalleVenta.dto;

import com.marco.shopProject.producto.dto.MostrarProductoDTO;
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
