package com.marco.shopProject.security.jwt.dto;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;

import java.util.List;

public record JwtTokenData(
        String value,
        Long userId,
        String email,
        List<String> roles,
        EstadoEnum estado,
        JwtTokenPurposeEnum purpose
) {
}
