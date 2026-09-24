package com.marco.shopProject.identity.auth.validator;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.identity.auth.dto.RefreshTokenValidado;
import com.marco.shopProject.identity.auth.entity.Token;
import com.marco.shopProject.identity.auth.exception.RefreshTokenInvalidoException;
import com.marco.shopProject.identity.auth.repository.TokenRepository;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.UsuarioEliminadoException;
import com.marco.shopProject.identity.user.repository.UserRepository;
import com.marco.shopProject.security.jwt.dto.JwtTokenData;
import com.marco.shopProject.core.tools.enums.JwtTokenPurposeEnum;
import com.marco.shopProject.security.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class RefreshTokenValidator {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    public RefreshTokenValidado validateRefreshToken(String authHeader){
        final JwtTokenData jwtToken;
        try {
            jwtToken = jwtService.parseBearerToken(authHeader);
        } catch (ExpiredJwtException exception) {
            throw new RefreshTokenInvalidoException(
                    "El refresh token ha expirado",
                    exception
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new RefreshTokenInvalidoException(
                    "El refresh token es inválido",
                    ex
            );
        }

        if (jwtToken.purpose() != JwtTokenPurposeEnum.REFRESH) {
            throw new RefreshTokenInvalidoException(
                    "El token recibido no es un refresh token"
            );
        }

        if(jwtToken.email() == null
                || jwtToken.email().isBlank()
                || jwtToken.userId() == null){
            throw new RefreshTokenInvalidoException(
                    "El refresh token no contiene un usuario válido"
            );
        }

        final User user = userRepository.findUserByEmail(jwtToken.email())
                .orElseThrow(() -> new RefreshTokenInvalidoException(
                        "No existe un usuario asociado al refresh token"
                ));

        if(user.getEstado() != EstadoEnum.ACTIVO){
            throw new UsuarioEliminadoException(
                    "El usuario esta actualmente eliminado"
            );
        }

        Token tokenGuardado = tokenRepository.findByToken(jwtToken.value())
                .orElseThrow(() -> new RefreshTokenInvalidoException(
                        "El refresh token no está registrado"
                ));

        if(tokenGuardado.isExpired() || tokenGuardado.isRevoked()){
            throw new RefreshTokenInvalidoException(
                    "El refresh token esta expirado o revocado"
            );
        }

        if(tokenGuardado.getUser() == null
                || !Objects.equals(user.getId(), jwtToken.userId())
                || !Objects.equals(user.getId(), tokenGuardado.getUser().getId())){
            throw new RefreshTokenInvalidoException(
                    "El refresh token no corresponde al usuario"
            );
        }

        return new RefreshTokenValidado(jwtToken.value(),user);
    }
}
