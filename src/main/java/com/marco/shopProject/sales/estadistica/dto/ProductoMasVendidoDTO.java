package com.marco.shopProject.sales.estadistica.dto;

import lombok.Builder;

@Builder
public record ProductoMasVendidoDTO(
        Long id,
        String nombre,
        Integer cantidad
) {
}
