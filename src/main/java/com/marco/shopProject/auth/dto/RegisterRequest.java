package com.marco.shopProject.auth.dto;

public record RegisterRequest(
        String email,
        String password,
        String name
) {
}
