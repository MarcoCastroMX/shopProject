package com.marco.shopProject.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
