package com.marco.shopProject.estadistica.dto;

import lombok.Builder;

@Builder
public record ProductoMasVendidoDTO(
        Long id,
        String nombre,
        Integer cantidad
) {
}
