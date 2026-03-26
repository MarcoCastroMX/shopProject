package com.marco.shopProject.auth.dto;

public record RegisterRequestDTO(
        String email,
        String password,
        String name
) {
}
