package com.marco.shopProject.security.jwt;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String generateToken(final User user){
        return buildToken(user, jwtExpiration, JwtTokenPurposeEnum.ACCESS);
    }

    public String generateRefreshToken(final User user){
        return buildToken(user, refreshExpiration, JwtTokenPurposeEnum.REFRESH);
    }

    private String buildToken(User user, long expiration, JwtTokenPurposeEnum purpose)
    {
        List<String> rolList = user.getRoles().stream()
                .map(Rol::getRol)
                .map(String::valueOf)
                .toList();

        return Jwts.builder()
                .id(user.getId().toString())
                .claim("name", user.getNombre())
                .claim("roles", rolList)
                .claim("estado", user.getEstado().name())
                .claim("token_type", purpose.name())
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenData parseBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Encabezado Bearer inválido");
        }

        String token = authHeader.substring(7);
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token vacío");
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String estado = claims.get("estado", String.class);
        String purpose = claims.get("token_type", String.class);
        List<?> rolesClaim = claims.get("roles", List.class);

        List<String> roles = rolesClaim == null
                ? List.of()
                : rolesClaim.stream()
                .map(String::valueOf)
                .toList();

        return new JwtTokenData(
                token,
                claims.getId() == null ? null : Long.valueOf(claims.getId()),
                claims.getSubject(),
                roles,
                estado == null ? null : EstadoEnum.valueOf(estado),
                purpose == null ? null : JwtTokenPurposeEnum.valueOf(purpose)
        );
    }
}
