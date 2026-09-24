package com.marco.shopProject.security.jwt.dto;

import java.util.List;

public record AccessTokenValidado(
        Long userId,
        String email,
        List<String> roles
) {
    public AccessTokenValidado {
        roles = List.copyOf(roles);
    }
}
