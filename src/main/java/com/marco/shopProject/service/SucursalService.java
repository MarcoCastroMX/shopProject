package com.marco.shopProject.service;

import com.marco.shopProject.dto.SucursalDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface SucursalService {
    List<SucursalDTO> getAllSucursales();
    SucursalDTO getSucursalById(Long id);
    SucursalDTO createSucursal(SucursalDTO newSucursal);
    SucursalDTO updateSucursal(Long id, SucursalDTO newSucursal);
    SucursalDTO patchSucursal(Long id, Map<String, Object> bodyArray);
    void deleteSucursal(Long id);
}
