package com.marco.shopProject.rol.service;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.mapper.Mapper;
import com.marco.shopProject.rol.dto.RolDTO;
import com.marco.shopProject.rol.entity.Rol;
import com.marco.shopProject.rol.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public List<RolDTO> obtenerRoles() {
        List<Rol> list = rolRepository.findAll();
        List<RolDTO> listDTO = list.stream()
                .map(Mapper::rolToRolDTO)
                .toList();
        return listDTO;
    }

    @Override
    public RolDTO crearRol(RolDTO rol) {
        Rol search = rolRepository.findRolByRol(rol.role());

        if(search != null){
            throw new RuntimeException("Rol ya existente");
        }
        Rol newRol = Rol.builder()
                .rol(rol.role())
                .users(new ArrayList<>())
                .build();

        rolRepository.save(newRol);

        return Mapper.rolToRolDTO(newRol);
    }

    @Override
    public RolDTO encontrarRolPorRolesEnum(RolesEnum role) {
        return Mapper.rolToRolDTO(rolRepository.findRolByRol(role));
    }
}
