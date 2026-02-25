package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record ProductoMasVendidoDTO(
        Long id,
        String nombre,
        Integer cantidad
) {
}
