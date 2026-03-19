package com.marco.shopProject.auth.service;

import com.marco.shopProject.auth.dto.LoginRequest;
import com.marco.shopProject.auth.dto.RegisterRequest;
import com.marco.shopProject.auth.dto.TokenResponse;
import com.marco.shopProject.auth.entity.Token;
import com.marco.shopProject.auth.repository.TokenRepository;
import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.entity.Rol;
import com.marco.shopProject.rol.repository.RolRepository;
import com.marco.shopProject.user.entity.User;
import com.marco.shopProject.user.exception.UserNotFoundException;
import com.marco.shopProject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request){
        var user = User.builder()
                .nombre(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .estado(EstadoEnum.ACTIVO)
                .build();

        Rol rol = rolRepository.findRolByRol(RolesEnum.ROLE_USER);
        user.add(rol);

        var savedUser = userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(savedUser,jwtToken);

        return new TokenResponse(jwtToken,refreshToken);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findUserByEmail(request.email())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user,jwtToken);
        return new TokenResponse(jwtToken,refreshToken);
    }

    private void revokeAllUserTokens(User user) {
        final List<Token> validUserTokens = tokenRepository.
                findAllExpiredIsFalseOrRevokedIsFalseByUserId(user.getId());
        if(!validUserTokens.isEmpty()){
            for(final Token token: validUserTokens){
                token.setRevoked(true);
                token.setExpired(true);
            }
            tokenRepository.saveAll(validUserTokens);
        }
    }

    private void saveUserToken(User user, String jwtToken){
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    public TokenResponse refreshToken(String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new IllegalArgumentException("Invalid Bearer Token");
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if(userEmail == null){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        final User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        if(!jwtService.isTokenValid(refreshToken,user)){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user,accessToken);
        return new TokenResponse(accessToken,refreshToken);

    }
}
