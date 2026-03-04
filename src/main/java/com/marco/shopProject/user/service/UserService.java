package com.marco.shopProject.user.service;

import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<MostrarUserDTO> obtenerUsuarios(String estado);
    List<MostrarUserDTO> obtenerUsuariosPorRol(String RolEnum, String estado);
    Boolean existeEmail(String email);
    MostrarUserDTO obtenerUsuarioPorId(Long id);
    MostrarUserDTO crearUsuario(CrearUserDTO user);
    MostrarUserDTO actualizacionParcialUsuario(Long id, Map<String, Object> body);
    void eliminarUsuario(Long id);
}
