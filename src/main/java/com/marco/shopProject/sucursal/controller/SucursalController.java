package com.marco.shopProject.sucursal.controller;

import com.marco.shopProject.sucursal.dto.SucursalDTO;
import com.marco.shopProject.sucursal.service.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SucursalController {

    private SucursalService sucursalService;

    @Autowired
    public SucursalController(SucursalService sucursalService){
        this.sucursalService = sucursalService;
    }

    @GetMapping("sucursales")
    public List<SucursalDTO> getAllSucursales(){
        return  sucursalService.getAllSucursales();
    }

    @GetMapping("sucursales/{id}")
    public SucursalDTO getSucursalById(@PathVariable Long id){
        return sucursalService.getSucursalById(id);
    }

    @PostMapping("sucursales")
    public SucursalDTO createSucursal(@RequestBody SucursalDTO newSucursal){
        return sucursalService.createSucursal(newSucursal);
    }

    @PutMapping("sucursales/{id}")
    public SucursalDTO updateSucursal(@PathVariable Long id, @RequestBody SucursalDTO newSucursal){
        return sucursalService.updateSucursal(id,newSucursal);
    }

    @PatchMapping("sucursales/{id}")
    public SucursalDTO patchSucursal(@PathVariable Long id, @RequestBody Map<String, Object> bodyArray){
        return sucursalService.patchSucursal(id,bodyArray);
}

    @DeleteMapping("sucursales/{id}")
    public String deleteSucursal(@PathVariable Long id){
        sucursalService.deleteSucursal(id);
        return "Sucursal Eliminada";
    }
}
