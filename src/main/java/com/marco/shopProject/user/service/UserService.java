package com.marco.shopProject.user.service;

import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;
import com.marco.shopProject.user.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<MostrarUserDTO> obtenerUsuarios();
    List<MostrarUserDTO> obtenerUsuariosPorRol(String RolEnum);
    Boolean existeEmail(String email);
    MostrarUserDTO obtenerUsuarioPorId(Long id);
    MostrarUserDTO crearUsuario(CrearUserDTO user);
    MostrarUserDTO actualizarUsuario(User newUser);
    MostrarUserDTO actualizacionParcialUsuario(Map<String, Object> body);
    void eliminarUsuario(Long id);
}
