package com.marco.shopProject.identity.user.unit;

import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.user.dto.CrearUserDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrearUserDTOTest {

    @Test
    void constructor_cuandoRolesEstanVacios_asignaRoleUser() {
        // Arrange
        List<RolesEnum> roles = List.of();

        // Act
        CrearUserDTO resultado = new CrearUserDTO(
                "Marco",
                "Perez",
                "marco@example.com",
                "Clave123!",
                "5512345678",
                roles
        );

        // Assert
        assertEquals(1, resultado.roles().size());
        assertEquals(RolesEnum.ROLE_USER, resultado.roles().get(0));
    }

    @Test
    void constructor_cuandoRolesSonNull_asignaRoleUser() {
        // Arrange
        List<RolesEnum> roles = null;

        // Act
        CrearUserDTO resultado = new CrearUserDTO(
                "Marco",
                "Perez",
                "marco@example.com",
                "Clave123!",
                "5512345678",
                roles
        );

        // Assert
        assertEquals(1, resultado.roles().size());
        assertEquals(RolesEnum.ROLE_USER, resultado.roles().get(0));
    }

    @Test
    void constructor_cuandoRecibeRolesExplicitos_conservaRoles() {
        // Arrange
        List<RolesEnum> roles = List.of(RolesEnum.ROLE_MANAGER);

        // Act
        CrearUserDTO resultado = new CrearUserDTO(
                "Marco",
                "Perez",
                "marco@example.com",
                "Clave123!",
                "5512345678",
                roles
        );

        // Assert
        assertEquals(1, resultado.roles().size());
        assertEquals(roles, resultado.roles());
    }
}
