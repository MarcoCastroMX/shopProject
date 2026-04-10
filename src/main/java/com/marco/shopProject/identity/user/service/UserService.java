package com.marco.shopProject.identity.user.service;

import com.marco.shopProject.identity.user.dto.CrearUserDTO;
import com.marco.shopProject.identity.user.dto.MostrarUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface UserService {
    Page<MostrarUserDTO> obtenerUsuarios(String estado, Pageable pageable);
    Page<MostrarUserDTO> obtenerUsuariosPorRol(String RolEnum, String estado, Pageable pageable);
    Boolean existeEmail(String email);
    MostrarUserDTO obtenerUsuarioPorId(Long id);
    MostrarUserDTO crearUsuario(CrearUserDTO user);
    MostrarUserDTO actualizacionParcialUsuario(Long id, Map<String, Object> body);
    void eliminarUsuario(Long id);
}
