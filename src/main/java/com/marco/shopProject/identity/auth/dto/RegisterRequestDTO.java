package com.marco.shopProject.identity.auth.dto;

public record RegisterRequestDTO(
        String email,
        String password,
        String name
) {
}
