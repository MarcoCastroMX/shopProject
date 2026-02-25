package com.marco.shopProject.venta.dto;

import com.marco.shopProject.detalleVenta.dto.CrearDetalleVentaDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CrearVentaDTO(
        String estado,
        Integer sucursalId,
        List<CrearDetalleVentaDTO> detalle
) {
}
