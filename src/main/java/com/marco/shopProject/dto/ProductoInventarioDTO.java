package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record ProductoInventarioDTO(
        Long id,
        String nombre,
        String categoria,
        Double precio,
        Integer cantidad
) {
}
