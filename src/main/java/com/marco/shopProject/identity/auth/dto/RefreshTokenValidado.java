package com.marco.shopProject.identity.auth.dto;

import com.marco.shopProject.identity.user.entity.User;

public record RefreshTokenValidado(
        String refreshToken,
        User usuario
) {
}
