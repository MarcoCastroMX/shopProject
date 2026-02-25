package com.marco.shopProject.producto.dto;

import lombok.Builder;

@Builder
public record MostrarProductoDTO(
        String nombre,
        Double precio
) {
}
