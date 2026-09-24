package com.marco.shopProject.identity.auth.unit;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.auth.dto.LoginRequestDTO;
import com.marco.shopProject.identity.auth.dto.RefreshTokenValidado;
import com.marco.shopProject.identity.auth.dto.RegisterRequestDTO;
import com.marco.shopProject.identity.auth.dto.TokenResponseDTO;
import com.marco.shopProject.identity.auth.entity.Token;
import com.marco.shopProject.identity.auth.repository.TokenRepository;
import com.marco.shopProject.identity.auth.service.AuthService;
import com.marco.shopProject.identity.auth.validator.RefreshTokenValidator;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.EmailAlreadyTakenException;
import com.marco.shopProject.identity.user.exception.UsuarioEliminadoException;
import com.marco.shopProject.identity.user.exception.UserNotFoundException;
import com.marco.shopProject.identity.user.repository.UserRepository;
import com.marco.shopProject.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    @Captor
    private ArgumentCaptor<User> usuarioCaptor;

    @Captor
    private ArgumentCaptor<Token> tokenCaptor;

    @Captor
    private ArgumentCaptor<List<Token>> tokensCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_cuandoDatosSonValidos_creaUsuarioGuardaRefreshTokenYRetornaTokens() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "marco@example.com",
                "Clave123!",
                "Marco"
        );
        Rol rolUser = crearRol(1L, RolesEnum.ROLE_USER);
        TokenResponseDTO esperado = new TokenResponseDTO(
                "Access Token",
                "Refresh Token"
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password()))
                .thenReturn("Contraseña codificada");
        when(rolRepository.findRolByRol(RolesEnum.ROLE_USER))
                .thenReturn(rolUser);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User usuario = invocation.getArgument(0);
                    usuario.setId(5L);
                    return usuario;
                });
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("Access Token");
        when(jwtService.generateRefreshToken(any(User.class)))
                .thenReturn("Refresh Token");

        // Act
        TokenResponseDTO resultado = authService.register(request);

        // Assert
        assertEquals(esperado, resultado);

        verify(userRepository).findUserByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(rolRepository).findRolByRol(RolesEnum.ROLE_USER);
        verify(userRepository).save(usuarioCaptor.capture());

        User usuarioGuardado = usuarioCaptor.getValue();

        assertEquals(5L, usuarioGuardado.getId());
        assertEquals(request.name(), usuarioGuardado.getNombre());
        assertEquals(request.email(), usuarioGuardado.getEmail());
        assertEquals("Contraseña codificada", usuarioGuardado.getPassword());
        assertEquals(EstadoEnum.ACTIVO, usuarioGuardado.getEstado());
        assertEquals(List.of(rolUser), usuarioGuardado.getRoles());

        verify(jwtService).generateToken(same(usuarioGuardado));
        verify(jwtService).generateRefreshToken(same(usuarioGuardado));
        verify(tokenRepository).save(tokenCaptor.capture());

        Token tokenGuardado = tokenCaptor.getValue();

        assertEquals("Refresh Token", tokenGuardado.getToken());
        assertEquals(Token.TokenType.BEARER, tokenGuardado.getTokenType());
        assertFalse(tokenGuardado.isExpired());
        assertFalse(tokenGuardado.isRevoked());
        assertSame(usuarioGuardado, tokenGuardado.getUser());
        assertEquals(1, usuarioGuardado.getTokens().size());
        assertSame(tokenGuardado, usuarioGuardado.getTokens().get(0));
    }

    @Test
    void register_cuandoEmailYaExiste_lanzaEmailAlreadyTakenException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "marco@example.com",
                "Clave123!",
                "Marco"
        );
        User usuarioExistente = crearUsuario(
                5L,
                request.email(),
                EstadoEnum.ACTIVO
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.of(usuarioExistente));

        // Act y Assert
        assertThrows(
                EmailAlreadyTakenException.class,
                () -> authService.register(request)
        );

        verify(userRepository).findUserByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(
                passwordEncoder,
                rolRepository,
                jwtService,
                tokenRepository
        );
    }

    @Test
    void login_cuandoCredencialesSonValidasYExistenTokensActivos_revocaTokensYRetornaNuevosTokens() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "Clave123!"
        );
        User usuario = crearUsuario(
                5L,
                request.email(),
                EstadoEnum.ACTIVO
        );
        Token tokenAnterior = crearToken(
                10L,
                "Refresh Token Anterior",
                usuario,
                false,
                false
        );
        List<Token> tokensActivos = new ArrayList<>(List.of(tokenAnterior));
        TokenResponseDTO esperado = new TokenResponseDTO(
                "Access Token",
                "Refresh Token"
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario))
                .thenReturn("Access Token");
        when(jwtService.generateRefreshToken(usuario))
                .thenReturn("Refresh Token");
        when(tokenRepository.findAllExpiredIsFalseOrRevokedIsFalseByUserId(
                usuario.getId()
        )).thenReturn(tokensActivos);

        // Act
        TokenResponseDTO resultado = authService.login(request);

        // Assert
        assertEquals(esperado, resultado);

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );

        verify(userRepository).findUserByEmail(request.email());
        verify(jwtService).generateToken(same(usuario));
        verify(jwtService).generateRefreshToken(same(usuario));
        verify(tokenRepository)
                .findAllExpiredIsFalseOrRevokedIsFalseByUserId(usuario.getId());
        verify(tokenRepository).saveAll(tokensCaptor.capture());

        List<Token> tokensRevocados = tokensCaptor.getValue();

        assertEquals(1, tokensRevocados.size());
        assertSame(tokenAnterior, tokensRevocados.get(0));
        assertTrue(tokenAnterior.isExpired());
        assertTrue(tokenAnterior.isRevoked());

        verify(tokenRepository).save(tokenCaptor.capture());

        Token nuevoToken = tokenCaptor.getValue();

        assertEquals("Refresh Token", nuevoToken.getToken());
        assertEquals(Token.TokenType.BEARER, nuevoToken.getTokenType());
        assertFalse(nuevoToken.isExpired());
        assertFalse(nuevoToken.isRevoked());
        assertSame(usuario, nuevoToken.getUser());
        assertEquals(2, usuario.getTokens().size());
        assertTrue(usuario.getTokens().contains(tokenAnterior));
        assertTrue(usuario.getTokens().contains(nuevoToken));
    }

    @Test
    void login_cuandoUsuarioNoTieneTokensActivosPrevios_guardaNuevoRefreshTokenYRetornaTokens() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "Clave123!"
        );
        User usuario = crearUsuario(
                5L,
                request.email(),
                EstadoEnum.ACTIVO
        );
        Token tokenHistorico = crearToken(
                10L,
                "Refresh Token Histórico",
                usuario,
                true,
                true
        );
        TokenResponseDTO esperado = new TokenResponseDTO(
                "Access Token",
                "Refresh Token"
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario))
                .thenReturn("Access Token");
        when(jwtService.generateRefreshToken(usuario))
                .thenReturn("Refresh Token");
        when(tokenRepository.findAllExpiredIsFalseOrRevokedIsFalseByUserId(
                usuario.getId()
        )).thenReturn(List.of());

        // Act
        TokenResponseDTO resultado = authService.login(request);

        // Assert
        assertEquals(esperado, resultado);

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );

        verify(userRepository).findUserByEmail(request.email());
        verify(jwtService).generateToken(same(usuario));
        verify(jwtService).generateRefreshToken(same(usuario));
        verify(tokenRepository)
                .findAllExpiredIsFalseOrRevokedIsFalseByUserId(usuario.getId());
        verify(tokenRepository, never()).saveAll(any());
        verify(tokenRepository).save(tokenCaptor.capture());

        Token nuevoToken = tokenCaptor.getValue();

        assertEquals("Refresh Token", nuevoToken.getToken());
        assertEquals(Token.TokenType.BEARER, nuevoToken.getTokenType());
        assertFalse(nuevoToken.isExpired());
        assertFalse(nuevoToken.isRevoked());
        assertSame(usuario, nuevoToken.getUser());
        assertEquals(2, usuario.getTokens().size());
        assertSame(tokenHistorico, usuario.getTokens().get(0));
        assertSame(nuevoToken, usuario.getTokens().get(1));
    }

    @Test
    void login_cuandoCredencialesSonInvalidas_lanzaBadCredentialsException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "ClaveIncorrecta1"
        );
        BadCredentialsException excepcionEsperada =
                new BadCredentialsException("Credenciales inválidas");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(excepcionEsperada);

        // Act y Assert
        BadCredentialsException resultado = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertSame(excepcionEsperada, resultado);

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );

        verifyNoInteractions(userRepository, jwtService, tokenRepository);
    }

    @Test
    void login_cuandoAutenticacionEsValidaPeroUsuarioNoExiste_lanzaUserNotFoundException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "Clave123!"
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );
        verify(userRepository).findUserByEmail(request.email());
        verifyNoInteractions(jwtService, tokenRepository);
    }

    @Test
    void login_cuandoUsuarioEncontradoEstaEliminado_lanzaUsuarioEliminadoException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "Clave123!"
        );
        User usuarioEliminado = crearUsuario(
                5L,
                request.email(),
                EstadoEnum.ELIMINADO
        );

        when(userRepository.findUserByEmail(request.email()))
                .thenReturn(Optional.of(usuarioEliminado));

        // Act y Assert
        assertThrows(
                UsuarioEliminadoException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );
        verify(userRepository).findUserByEmail(request.email());
        verifyNoInteractions(jwtService, tokenRepository);
    }

    @Test
    void login_cuandoAuthenticationManagerDetectaUsuarioDeshabilitado_lanzaUsuarioEliminadoException() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "marco@example.com",
                "Clave123!"
        );
        DisabledException disabledException =
                new DisabledException("Usuario deshabilitado");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(disabledException);

        // Act y Assert
        UsuarioEliminadoException resultado = assertThrows(
                UsuarioEliminadoException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "El usuario se encuentra eliminado y no puede iniciar sesión",
                resultado.getMessage()
        );
        assertSame(disabledException, resultado.getCause());

        verify(authenticationManager).authenticate(
                authenticationCaptor.capture()
        );
        assertEquals(
                request.email(),
                authenticationCaptor.getValue().getPrincipal()
        );
        assertEquals(
                request.password(),
                authenticationCaptor.getValue().getCredentials()
        );

        verifyNoInteractions(userRepository, jwtService, tokenRepository);
    }

    @Test
    void refreshToken_cuandoRefreshTokenEsValido_generaNuevoAccessTokenYConservaRefreshToken() {
        // Arrange
        String authHeader = "Bearer Refresh Token";
        User usuario = crearUsuario(
                5L,
                "marco@example.com",
                EstadoEnum.ACTIVO
        );
        RefreshTokenValidado validado = new RefreshTokenValidado(
                "Refresh Token",
                usuario
        );
        TokenResponseDTO esperado = new TokenResponseDTO(
                "Nuevo Access Token",
                "Refresh Token"
        );

        when(refreshTokenValidator.validateRefreshToken(authHeader))
                .thenReturn(validado);
        when(jwtService.generateToken(usuario))
                .thenReturn("Nuevo Access Token");

        // Act
        TokenResponseDTO resultado = authService.refreshToken(authHeader);

        // Assert
        assertEquals(esperado, resultado);
        verify(refreshTokenValidator).validateRefreshToken(authHeader);
        verify(jwtService).generateToken(same(usuario));
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

    private Rol crearRol(Long id, RolesEnum rol) {
        return Rol.builder()
                .id(id)
                .rol(rol)
                .build();
    }

    private Token crearToken(
            Long id,
            String valor,
            User usuario,
            boolean expired,
            boolean revoked
    ) {
        Token token = Token.builder()
                .id(id)
                .token(valor)
                .tokenType(Token.TokenType.BEARER)
                .expired(expired)
                .revoked(revoked)
                .user(usuario)
                .build();

        usuario.addToken(token);
        return token;
    }
}
