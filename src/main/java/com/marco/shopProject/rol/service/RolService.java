package com.marco.shopProject.rol.service;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.dto.RolDTO;

import java.util.List;

public interface RolService {
    List<RolDTO> obtenerRoles();
    RolDTO crearRol(RolDTO rol);
    RolDTO encontrarRolPorRolesEnum(RolesEnum role);
}
