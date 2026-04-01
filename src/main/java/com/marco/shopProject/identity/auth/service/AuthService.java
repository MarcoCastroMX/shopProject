package com.marco.shopProject.identity.auth.service;

import com.marco.shopProject.identity.auth.dto.LoginRequestDTO;
import com.marco.shopProject.identity.auth.dto.RegisterRequestDTO;
import com.marco.shopProject.identity.auth.dto.TokenResponseDTO;
import com.marco.shopProject.identity.auth.entity.Token;
import com.marco.shopProject.identity.auth.repository.TokenRepository;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.security.jwt.JwtService;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.UserNotFoundException;
import com.marco.shopProject.identity.user.repository.UserRepository;
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

    public TokenResponseDTO register(RegisterRequestDTO request){
        var user = User.builder()
                .nombre(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .estado(EstadoEnum.ACTIVO)
                .build();

        Rol rol = rolRepository.findRolByRol(RolesEnum.ROLE_USER);
        user.addRol(rol);

        var savedUser = userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(savedUser,refreshToken);

        return new TokenResponseDTO(jwtToken,refreshToken);
    }

    public TokenResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email(),
                                    request.password()
                            )
                    );

        var user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User No Encontrado"));

        if(user.getEstado() != EstadoEnum.ACTIVO){
            throw new RuntimeException("Usuario Eliminado");
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user,refreshToken);
        return new TokenResponseDTO(jwtToken,refreshToken);
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

        user.addToken(token);
        tokenRepository.save(token);
    }

    public TokenResponseDTO refreshToken(String authHeader) {
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

        if(!jwtService.isTokenValid(refreshToken,user.getEmail())){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        final String accessToken = jwtService.generateToken(user);

        return new TokenResponseDTO(accessToken,refreshToken);

    }
}
