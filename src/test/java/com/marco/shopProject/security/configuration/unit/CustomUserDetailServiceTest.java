package com.marco.shopProject.security.configuration.unit;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.repository.UserRepository;
import com.marco.shopProject.security.configuration.CustomUserDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    void loadUserByUsername_cuandoUsuarioActivoExiste_retornaUserDetailsHabilitado() {
        // Arrange
        String email = "marco@example.com";
        User usuario = crearUsuario(email, EstadoEnum.ACTIVO);

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails resultado =
                customUserDetailService.loadUserByUsername(email);

        // Assert
        assertEquals(usuario.getEmail(), resultado.getUsername());
        assertEquals(usuario.getPassword(), resultado.getPassword());
        assertEquals(1, resultado.getAuthorities().size());
        assertEquals(
                RolesEnum.ROLE_USER.name(),
                resultado.getAuthorities().iterator().next().getAuthority()
        );
        assertTrue(resultado.isEnabled());

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void loadUserByUsername_cuandoUsuarioNoExiste_lanzaUsernameNotFoundException() {
        // Arrange
        String email = "inexistente@example.com";

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailService.loadUserByUsername(email)
        );

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void loadUserByUsername_cuandoUsuarioEstaEliminado_retornaUserDetailsDeshabilitado() {
        // Arrange
        String email = "eliminado@example.com";
        User usuario = crearUsuario(email, EstadoEnum.ELIMINADO);

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails resultado =
                customUserDetailService.loadUserByUsername(email);

        // Assert
        assertEquals(usuario.getEmail(), resultado.getUsername());
        assertEquals(usuario.getPassword(), resultado.getPassword());
        assertEquals(1, resultado.getAuthorities().size());
        assertEquals(
                RolesEnum.ROLE_USER.name(),
                resultado.getAuthorities().iterator().next().getAuthority()
        );
        assertFalse(resultado.isEnabled());

        verify(userRepository).findUserByEmail(email);
    }

    private User crearUsuario(String email, EstadoEnum estado) {
        Rol rolUser = Rol.builder()
                .id(1L)
                .rol(RolesEnum.ROLE_USER)
                .users(new ArrayList<>())
                .build();

        return User.builder()
                .id(5L)
                .nombre("Marco")
                .email(email)
                .password("Contraseña codificada")
                .estado(estado)
                .roles(List.of(rolUser))
                .tokens(new ArrayList<>())
                .build();
    }
}
