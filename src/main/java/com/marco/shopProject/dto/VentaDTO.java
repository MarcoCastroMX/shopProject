package com.marco.shopProject.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record VentaDTO(
        Long id,
        LocalDateTime fecha,
        String estado,
        Long sucursalId,
        List<DetalleVentaDTO> detalle,
        Double total
) {
}
