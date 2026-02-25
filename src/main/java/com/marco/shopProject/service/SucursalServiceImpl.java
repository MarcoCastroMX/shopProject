package com.marco.shopProject.service;

import com.marco.shopProject.dto.SucursalDTO;
import com.marco.shopProject.entity.Sucursal;
import com.marco.shopProject.exception.SucursalNoEncontradaException;
import com.marco.shopProject.mapper.Mapper;
import com.marco.shopProject.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
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
    public List<SucursalDTO> getAllSucursales() {
        return sucursalRepository.findAll().stream().map(Mapper::toDTO).toList();
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
