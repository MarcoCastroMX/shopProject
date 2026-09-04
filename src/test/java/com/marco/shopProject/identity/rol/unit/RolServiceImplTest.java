package com.marco.shopProject.identity.rol.unit;

import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.dto.RolDTO;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.identity.rol.service.RolServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    void obtenerRoles_cuandoExistenRoles_retornaListaDeRolesDTO() {
        // Arrange
        Rol rolAdmin = Rol.builder()
                .id(1L)
                .rol(RolesEnum.ROLE_ADMIN)
                .build();
        Rol rolUser = Rol.builder()
                .id(2L)
                .rol(RolesEnum.ROLE_USER)
                .build();

        List<Rol> roles = List.of(rolAdmin, rolUser);
        List<RolDTO> esperado = List.of(
                new RolDTO(RolesEnum.ROLE_ADMIN),
                new RolDTO(RolesEnum.ROLE_USER)
        );

        when(rolRepository.findAll()).thenReturn(roles);

        // Act
        List<RolDTO> resultado = rolService.obtenerRoles();

        // Assert
        assertEquals(esperado, resultado);
        verify(rolRepository).findAll();
    }

    @Test
    void obtenerRoles_cuandoNoExistenRoles_retornaListaVacia() {
        // Arrange
        when(rolRepository.findAll()).thenReturn(List.of());

        // Act
        List<RolDTO> resultado = rolService.obtenerRoles();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(rolRepository).findAll();
    }
}
