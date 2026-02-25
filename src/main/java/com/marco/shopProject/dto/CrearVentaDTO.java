package com.marco.shopProject.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CrearVentaDTO(
        String estado,
        Integer sucursalId,
        List<CrearDetalleVentaDTO> detalle
) {
}
