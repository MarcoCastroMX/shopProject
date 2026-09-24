package com.marco.shopProject.security.jwt;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.security.jwt.dto.AccessTokenValidado;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccessTokenValidator {

    private final JwtService jwtService;

    public Optional<AccessTokenValidado> validateAccessToken(String authHeader) {
        try {
            JwtTokenData token = jwtService.parseBearerToken(authHeader);

            if (token.purpose() != JwtTokenPurposeEnum.ACCESS
                    || token.userId() == null
                    || token.email() == null
                    || token.email().isBlank()
                    || token.estado() != EstadoEnum.ACTIVO
                    || token.roles().isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new AccessTokenValidado(
                    token.userId(),
                    token.email(),
                    token.roles()
            ));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
