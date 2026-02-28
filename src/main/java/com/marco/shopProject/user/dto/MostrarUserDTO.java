package com.marco.shopProject.user.dto;

import lombok.Builder;

@Builder
public record MostrarUserDTO(
        Long id,
        String email
) {
}
