package com.marco.shopProject.identity.rol.service;

import com.marco.shopProject.core.tools.mapper.Mapper;
import com.marco.shopProject.identity.rol.dto.RolDTO;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import org.springframework.stereotype.Service;

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
}
