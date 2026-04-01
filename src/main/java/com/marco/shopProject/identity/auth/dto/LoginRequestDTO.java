package com.marco.shopProject.identity.auth.dto;

public record LoginRequestDTO(
        String email,
        String password
) {
}
