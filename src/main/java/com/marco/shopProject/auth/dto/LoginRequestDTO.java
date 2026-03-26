package com.marco.shopProject.auth.dto;

public record LoginRequestDTO(
        String email,
        String password
) {
}
