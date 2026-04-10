package com.marco.shopProject.catalog.sucursal.service;

import com.marco.shopProject.catalog.sucursal.dto.SucursalDTO;
import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import com.marco.shopProject.catalog.sucursal.exception.SucursalNoEncontradaException;
import com.marco.shopProject.core.tools.mapper.Mapper;
import com.marco.shopProject.catalog.sucursal.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final JsonMapper jsonMapper;

    @Autowired
    public SucursalServiceImpl(SucursalRepository sucursalRepository, JsonMapper jsonMapper){
        this.sucursalRepository = sucursalRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Page<SucursalDTO> getAllSucursales(Pageable pageable) {
        Page<Sucursal> sucursales = sucursalRepository.findAll(pageable);
        return sucursales.map(Mapper::toDTO);
    }

    @Override
    public SucursalDTO getSucursalById(Long id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new SucursalNoEncontradaException(id));

        return Mapper.toDTO(sucursal);
    }

    @Override
    public SucursalDTO createSucursal(SucursalDTO newSucursal) {
        Sucursal sucursal = Mapper.toDTO(newSucursal);
        return Mapper.toDTO(sucursalRepository.save(sucursal));
    }

    @Override
    public SucursalDTO updateSucursal(Long id, SucursalDTO newSucursal) {
        Sucursal old = sucursalRepository.findById(id)
                .orElseThrow(() -> new SucursalNoEncontradaException(id));

        old.setNombre(newSucursal.nombre());
        old.setDireccion(newSucursal.direccion());

        return Mapper.toDTO(sucursalRepository.save(old));
    }

    @Override
    public SucursalDTO patchSucursal(Long id, Map<String, Object> bodyArray) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new SucursalNoEncontradaException(id));

        bodyArray.remove("id");

        Sucursal newSucursal = jsonMapper.updateValue(sucursal,bodyArray);
        return Mapper.toDTO(newSucursal);
    }

    @Override
    public void deleteSucursal(Long id) {
        Sucursal old = sucursalRepository.findById(id)
                .orElseThrow(() -> new SucursalNoEncontradaException(id));

        sucursalRepository.delete(old);
    }
}
