package com.marco.shopProject.identity.user.dto;

import com.marco.shopProject.core.tools.enums.RolesEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record CrearUserDTO(
        @Size(max = 30, message = "El nombre no puede superar los 30 caracteres")
        String nombre,

        @Size(max = 30, message = "El apellido no puede superar los 30 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres")
        String password,

        @Pattern(regexp = "\\d{10}", message = "El telefono debe contener exactamente 10 digitos")
        String telefono,

        List<RolesEnum> roles
) {
    public CrearUserDTO {
        if(roles == null || roles.isEmpty()){
            roles = List.of(RolesEnum.ROLE_USER);
        }
    }
}
