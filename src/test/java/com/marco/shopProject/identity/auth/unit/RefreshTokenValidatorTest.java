package com.marco.shopProject.identity.auth.unit;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.auth.dto.RefreshTokenValidado;
import com.marco.shopProject.identity.auth.entity.Token;
import com.marco.shopProject.identity.auth.exception.RefreshTokenInvalidoException;
import com.marco.shopProject.identity.auth.repository.TokenRepository;
import com.marco.shopProject.identity.auth.validator.RefreshTokenValidator;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.UsuarioEliminadoException;
import com.marco.shopProject.identity.user.repository.UserRepository;
import com.marco.shopProject.security.jwt.JwtService;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenValidator refreshTokenValidator;

    @Test
    void validateRefreshToken_cuandoTokenYUsuarioSonValidos_retornaRefreshTokenValidado() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(5L, email, EstadoEnum.ACTIVO);
        Token tokenGuardado = crearToken(
                1L,
                usuario,
                false,
                false
        );
        RefreshTokenValidado esperado =
                new RefreshTokenValidado("Refresh Token", usuario);

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenValidado resultado =
                refreshTokenValidator.validateRefreshToken(authHeader);

        // Assert
        assertEquals(esperado, resultado);

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoJwtEstaExpirado_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token Caducado";
        Header header = mock(Header.class);
        Claims claims = mock(Claims.class);
        ExpiredJwtException expiredJwtException =
                new ExpiredJwtException(
                        header,
                        claims,
                        "Error técnico: JWT expirado"
                );

        when(jwtService.parseBearerToken(authHeader))
                .thenThrow(expiredJwtException);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals("El refresh token ha expirado", resultado.getMessage());
        assertSame(expiredJwtException, resultado.getCause());

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoJwtEsInvalido_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token Invalido";
        JwtException jwtException =
                new JwtException("Error técnico al analizar el JWT");

        when(jwtService.parseBearerToken(authHeader))
                .thenThrow(jwtException);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals("El refresh token es inválido", resultado.getMessage());
        assertSame(jwtException, resultado.getCause());

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoJwtServiceRechazaLaCabecera_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Refresh Token Sin Bearer";
        IllegalArgumentException illegalArgumentException =
                new IllegalArgumentException("Error en la cabecera");

        when(jwtService.parseBearerToken(authHeader))
                .thenThrow(illegalArgumentException);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals("El refresh token es inválido", resultado.getMessage());
        assertSame(illegalArgumentException, resultado.getCause());

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoTokenEsAccess_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Access Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "marco@example.com",
                JwtTokenPurposeEnum.ACCESS
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El token recibido no es un refresh token",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoTokenNoTieneEmail_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                null,
                JwtTokenPurposeEnum.REFRESH
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no contiene un usuario válido",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoEmailEstaEnBlanco_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "   ",
                JwtTokenPurposeEnum.REFRESH
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no contiene un usuario válido",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoTokenNoTieneUserId_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                null,
                "marco@example.com",
                JwtTokenPurposeEnum.REFRESH
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no contiene un usuario válido",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verifyNoInteractions(userRepository, tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoUsuarioNoExiste_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                "inexistente@example.com",
                JwtTokenPurposeEnum.REFRESH
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(jwtToken.email()))
                .thenReturn(Optional.empty());

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "No existe un usuario asociado al refresh token",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoUsuarioEstaEliminado_lanzaUsuarioEliminadoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "eliminado@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuarioEliminado = crearUsuario(
                5L,
                email,
                EstadoEnum.ELIMINADO
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuarioEliminado));

        // Act
        UsuarioEliminadoException resultado = assertThrows(
                UsuarioEliminadoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El usuario esta actualmente eliminado",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void validateRefreshToken_cuandoRefreshTokenNoEstaRegistrado_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(5L, email, EstadoEnum.ACTIVO);

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.empty());

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no está registrado",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoTokenGuardadoEstaMarcadoComoExpirado_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(5L, email, EstadoEnum.ACTIVO);
        Token tokenGuardado = crearToken(
                1L,
                usuario,
                true,
                false
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token esta expirado o revocado",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoTokenGuardadoEstaMarcadoComoRevocado_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(5L, email, EstadoEnum.ACTIVO);
        Token tokenGuardado = crearToken(
                1L,
                usuario,
                false,
                true
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token esta expirado o revocado",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoTokenGuardadoNoTieneUsuario_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(5L, email, EstadoEnum.ACTIVO);
        Token tokenGuardado = crearToken(
                1L,
                null,
                false,
                false
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no corresponde al usuario",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoIdUsuarioNoCoincideConIdJwt_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuario = crearUsuario(6L, email, EstadoEnum.ACTIVO);
        Token tokenGuardado = crearToken(
                1L,
                usuario,
                false,
                false
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no corresponde al usuario",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    @Test
    void validateRefreshToken_cuandoTokenGuardadoPerteneceAOtroUsuario_lanzaRefreshTokenInvalidoException() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        String email = "marco@example.com";
        JwtTokenData jwtToken = crearJwtTokenData(
                5L,
                email,
                JwtTokenPurposeEnum.REFRESH
        );
        User usuarioEncontrado = crearUsuario(
                5L,
                email,
                EstadoEnum.ACTIVO
        );
        User otroUsuario = crearUsuario(
                6L,
                "otro@example.com",
                EstadoEnum.ACTIVO
        );
        Token tokenGuardado = crearToken(
                1L,
                otroUsuario,
                false,
                false
        );

        when(jwtService.parseBearerToken(authHeader))
                .thenReturn(jwtToken);
        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuarioEncontrado));
        when(tokenRepository.findByToken(jwtToken.value()))
                .thenReturn(Optional.of(tokenGuardado));

        // Act
        RefreshTokenInvalidoException resultado = assertThrows(
                RefreshTokenInvalidoException.class,
                () -> refreshTokenValidator.validateRefreshToken(authHeader)
        );

        // Assert
        assertEquals(
                "El refresh token no corresponde al usuario",
                resultado.getMessage()
        );

        verify(jwtService).parseBearerToken(authHeader);
        verify(userRepository).findUserByEmail(jwtToken.email());
        verify(tokenRepository).findByToken(jwtToken.value());
    }

    private JwtTokenData crearJwtTokenData(
            Long userId,
            String email,
            JwtTokenPurposeEnum purpose
    ) {
        return new JwtTokenData(
                "Refresh Token",
                userId,
                email,
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO,
                purpose
        );
    }

    private User crearUsuario(
            Long id,
            String email,
            EstadoEnum estado
    ) {
        return User.builder()
                .id(id)
                .nombre("Marco")
                .email(email)
                .password("Contraseña codificada")
                .estado(estado)
                .roles(new ArrayList<>())
                .tokens(new ArrayList<>())
                .build();
    }

    private Token crearToken(
            Long id,
            User usuario,
            boolean expired,
            boolean revoked
    ) {
        return Token.builder()
                .id(id)
                .token("Refresh Token")
                .tokenType(Token.TokenType.BEARER)
                .expired(expired)
                .revoked(revoked)
                .user(usuario)
                .build();
    }
}
