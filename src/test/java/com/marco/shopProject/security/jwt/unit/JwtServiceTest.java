package com.marco.shopProject.security.jwt.unit;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.security.jwt.JwtService;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final long ACCESS_EXPIRATION = 60_000L;
    private static final long REFRESH_EXPIRATION = 300_000L;
    private static final long DATE_TOLERANCE_MILLIS = 1_000L;

    @Test
    void generateToken_cuandoUsuarioEsValido_generaAccessTokenConClaimsYExpiracionCorrectos() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                ACCESS_EXPIRATION
        );
        User usuario = crearUsuarioValido();
        Instant limiteInferior = Instant.now().minusSeconds(1);

        // Act
        String token = jwtService.generateToken(usuario);

        // Assert
        Instant limiteSuperior = Instant.now().plusSeconds(1);
        Claims claims = obtenerClaims(token, key);

        verificarClaimsGenerados(
                claims,
                usuario,
                JwtTokenPurposeEnum.ACCESS,
                ACCESS_EXPIRATION,
                limiteInferior,
                limiteSuperior
        );
    }

    @Test
    void generateRefreshToken_cuandoUsuarioEsValido_generaRefreshTokenConClaimsYExpiracionCorrectos() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        ReflectionTestUtils.setField(
                jwtService,
                "refreshExpiration",
                REFRESH_EXPIRATION
        );
        User usuario = crearUsuarioValido();
        Instant limiteInferior = Instant.now().minusSeconds(1);

        // Act
        String token = jwtService.generateRefreshToken(usuario);

        // Assert
        Instant limiteSuperior = Instant.now().plusSeconds(1);
        Claims claims = obtenerClaims(token, key);

        verificarClaimsGenerados(
                claims,
                usuario,
                JwtTokenPurposeEnum.REFRESH,
                REFRESH_EXPIRATION,
                limiteInferior,
                limiteSuperior
        );
    }

    @Test
    void parseBearerToken_cuandoTokenEsValidoYTieneIdEstadoYProposito_retornaJwtTokenData() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        List<String> roles = List.of(
                RolesEnum.ROLE_USER.name(),
                RolesEnum.ROLE_MANAGER.name()
        );
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                roles,
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );
        JwtTokenData esperado = new JwtTokenData(
                token,
                5L,
                "marco@example.com",
                roles,
                EstadoEnum.ACTIVO,
                JwtTokenPurposeEnum.ACCESS
        );

        // Act
        JwtTokenData resultado =
                jwtService.parseBearerToken("Bearer " + token);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    void parseBearerToken_cuandoEncabezadoEsNulo_lanzaIllegalArgumentException() {
        // Arrange
        JwtService jwtService = crearJwtService(
                Jwts.SIG.HS256.key().build()
        );

        // Act
        IllegalArgumentException resultado = assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parseBearerToken(null)
        );

        // Assert
        assertEquals("Encabezado Bearer inválido", resultado.getMessage());
    }

    @Test
    void parseBearerToken_cuandoEncabezadoNoIniciaConBearer_lanzaIllegalArgumentException() {
        // Arrange
        JwtService jwtService = crearJwtService(
                Jwts.SIG.HS256.key().build()
        );

        // Act
        IllegalArgumentException resultado = assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parseBearerToken("Access Token")
        );

        // Assert
        assertEquals("Encabezado Bearer inválido", resultado.getMessage());
    }

    @Test
    void parseBearerToken_cuandoEncabezadoSoloDiceBearer_lanzaIllegalArgumentException() {
        // Arrange
        JwtService jwtService = crearJwtService(
                Jwts.SIG.HS256.key().build()
        );

        // Act
        IllegalArgumentException resultado = assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parseBearerToken("Bearer ")
        );

        // Assert
        assertEquals("Token vacío", resultado.getMessage());
    }

    @Test
    void parseBearerToken_cuandoTokenTieneFirmaDistinta_lanzaSignatureException() {
        // Arrange
        SecretKey serviceKey = Jwts.SIG.HS256.key().build();
        SecretKey otherKey = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(serviceKey);
        String token = crearTokenFirmado(
                otherKey,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act y Assert
        assertThrows(
                SignatureException.class,
                () -> jwtService.parseBearerToken("Bearer " + token)
        );
    }

    @Test
    void parseBearerToken_cuandoTokenEstaExpirado_lanzaExpiredJwtException() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        Date fechaExpirada = new Date(
                System.currentTimeMillis() - 60_000L
        );
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaExpirada
        );

        // Act y Assert
        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.parseBearerToken("Bearer " + token)
        );
    }

    @Test
    void parseBearerToken_cuandoRolesEsNulo_retornaJwtTokenConListaVaciaEnRoles() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                null,
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act
        JwtTokenData resultado =
                jwtService.parseBearerToken("Bearer " + token);

        // Assert
        assertNotNull(resultado.roles());
        assertTrue(resultado.roles().isEmpty());
    }

    @Test
    void parseBearerToken_cuandoIdEsNulo_retornaJwtTokenConIdNulo() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                null,
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act
        JwtTokenData resultado =
                jwtService.parseBearerToken("Bearer " + token);

        // Assert
        assertNull(resultado.userId());
    }

    @Test
    void parseBearerToken_cuandoIdEsDiferenteALong_lanzaNumberFormatException() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "id-no-numerico",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act y Assert
        assertThrows(
                NumberFormatException.class,
                () -> jwtService.parseBearerToken("Bearer " + token)
        );
    }

    @Test
    void parseBearerToken_cuandoEstadoEsNulo_retornaJwtTokenConEstadoNulo() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                null,
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act
        JwtTokenData resultado =
                jwtService.parseBearerToken("Bearer " + token);

        // Assert
        assertNull(resultado.estado());
    }

    @Test
    void parseBearerToken_cuandoEstadoNoEsConvertibleAEstadoEnum_lanzaIllegalArgumentException() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                "SUSPENDIDO",
                JwtTokenPurposeEnum.ACCESS.name(),
                fechaFutura()
        );

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parseBearerToken("Bearer " + token)
        );
    }

    @Test
    void parseBearerToken_cuandoPropositoEsNulo_retornaJwtTokenConPropositoNulo() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                null,
                fechaFutura()
        );

        // Act
        JwtTokenData resultado =
                jwtService.parseBearerToken("Bearer " + token);

        // Assert
        assertNull(resultado.purpose());
    }

    @Test
    void parseBearerToken_cuandoPropositoNoEsConvertibleJwtTokenPurposeEnum_lanzaIllegalArgumentException() {
        // Arrange
        SecretKey key = Jwts.SIG.HS256.key().build();
        JwtService jwtService = crearJwtService(key);
        String token = crearTokenFirmado(
                key,
                "5",
                "marco@example.com",
                List.of(RolesEnum.ROLE_USER.name()),
                EstadoEnum.ACTIVO.name(),
                "SESSION",
                fechaFutura()
        );

        // Act y Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.parseBearerToken("Bearer " + token)
        );
    }

    @Test
    void parseBearerToken_cuandoJwtEstaMalFormado_lanzaMalformedJwtException() {
        // Arrange
        JwtService jwtService = crearJwtService(
                Jwts.SIG.HS256.key().build()
        );

        // Act y Assert
        assertThrows(
                MalformedJwtException.class,
                () -> jwtService.parseBearerToken(
                        "Bearer jwt-sin-estructura-valida"
                )
        );
    }

    private JwtService crearJwtService(SecretKey key) {
        JwtService jwtService = new JwtService();
        String secretKey = Encoders.BASE64.encode(key.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        return jwtService;
    }

    private User crearUsuarioValido() {
        Rol rol = Rol.builder()
                .id(1L)
                .rol(RolesEnum.ROLE_USER)
                .build();

        return User.builder()
                .id(5L)
                .nombre("Marco")
                .apellido("Pérez")
                .email("marco@example.com")
                .password("Contraseña codificada")
                .telefono("1234567890")
                .estado(EstadoEnum.ACTIVO)
                .roles(List.of(rol))
                .build();
    }

    private Claims obtenerClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String crearTokenFirmado(
            SecretKey key,
            String id,
            String email,
            List<?> roles,
            String estado,
            String purpose,
            Date expiration
    ) {
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(expiration);

        if (id != null) {
            builder.id(id);
        }
        if (roles != null) {
            builder.claim("roles", roles);
        }
        if (estado != null) {
            builder.claim("estado", estado);
        }
        if (purpose != null) {
            builder.claim("token_type", purpose);
        }

        return builder.signWith(key).compact();
    }

    private Date fechaFutura() {
        return new Date(System.currentTimeMillis() + 300_000L);
    }

    private void verificarClaimsGenerados(
            Claims claims,
            User usuario,
            JwtTokenPurposeEnum purpose,
            long expiration,
            Instant limiteInferior,
            Instant limiteSuperior
    ) {
        assertEquals(usuario.getId().toString(), claims.getId());
        assertEquals(usuario.getEmail(), claims.getSubject());
        assertEquals(usuario.getNombre(), claims.get("name", String.class));
        assertEquals(
                usuario.getEstado().name(),
                claims.get("estado", String.class)
        );
        assertEquals(
                purpose.name(),
                claims.get("token_type", String.class)
        );

        List<?> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertEquals(usuario.getRoles().size(), roles.size());
        assertEquals(RolesEnum.ROLE_USER.name(), roles.getFirst());

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiracionReal = claims.getExpiration().toInstant();
        Instant expiracionEsperada = issuedAt.plusMillis(expiration);

        assertFalse(issuedAt.isBefore(limiteInferior));
        assertFalse(issuedAt.isAfter(limiteSuperior));

        long diferencia = Math.abs(
                Duration.between(
                        expiracionEsperada,
                        expiracionReal
                ).toMillis()
        );
        assertTrue(diferencia <= DATE_TOLERANCE_MILLIS);
    }
}
