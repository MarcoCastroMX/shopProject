package com.marco.shopProject.security.jwt.unit;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.security.jwt.AccessTokenValidator;
import com.marco.shopProject.security.jwt.JwtService;
import com.marco.shopProject.security.jwt.dto.AccessTokenValidado;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenValidatorTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccessTokenValidator accessTokenValidator;

    @Test
    void validateAccessToken_cuandoAccessTokenEsValido_retornaOptionalConAccessTokenValidado() {
        // Arrange
        String authHeader = "Bearer Access Token";
        List<String> roles = List.of(RolesEnum.ROLE_USER.name());
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "marco@example.com",
                roles,
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );
        AccessTokenValidado esperado = new AccessTokenValidado(
                5L,
                "marco@example.com",
                roles
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(esperado, resultado.orElseThrow());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoJwtServiceRechazaLaCabecera_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Access Token Sin Bearer";
        IllegalArgumentException exception =
                new IllegalArgumentException("Encabezado Bearer inválido");

        when(jwtService.parseBearerToken(authHeader))
                .thenThrow(exception);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoJwtServiceLanzaJwtException_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token Invalido";
        JwtException exception =
                new JwtException("Error técnico al analizar el JWT");

        when(jwtService.parseBearerToken(authHeader))
                .thenThrow(exception);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoTokenEsRefresh_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.REFRESH
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoTokenNoTieneUserId_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                null,
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoTokenNoTieneEmail_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                null,
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoEmailEstaEnBlanco_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "   ",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoUsuarioEstaEliminado_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ELIMINADO,
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    @Test
    void validateAccessToken_cuandoNoContieneRoles_retornaOptionalVacio() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "marco@example.com",
                List.of(),
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(authHeader);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(jwtService).parseBearerToken(authHeader);
    }

    private JwtTokenData crearJwtTokenData(
            Long userId,
            String email,
            List<String> roles,
            EstadoEnum estado,
            JwtTokenPurposeEnum purpose
    ) {
        return new JwtTokenData(
                "Access Token",
                userId,
                email,
                roles,
                estado,
                purpose
        );
    }
}
