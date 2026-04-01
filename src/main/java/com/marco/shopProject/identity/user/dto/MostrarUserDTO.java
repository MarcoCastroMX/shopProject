package com.marco.shopProject.identity.user.dto;

import lombok.Builder;

@Builder
public record MostrarUserDTO(
        Long id,
        String email,
        String estado
) {
}
