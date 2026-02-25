package com.marco.shopProject.dto;

import lombok.Builder;

@Builder
public record MostrarProductoDTO(
        String nombre,
        Double precio
) {
}
