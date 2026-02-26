package com.marco.shopProject.sucursal.controller;

import com.marco.shopProject.sucursal.dto.SucursalDTO;
import com.marco.shopProject.sucursal.service.SucursalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    public ResponseEntity<List<SucursalDTO>> getAllSucursales(){
        return  ResponseEntity.ok(sucursalService.getAllSucursales());
    }

    @GetMapping("sucursales/{id}")
    public ResponseEntity<SucursalDTO> getSucursalById(@PathVariable Long id){
        return ResponseEntity.ok(sucursalService.getSucursalById(id));
    }

    @PostMapping("sucursales")
    public ResponseEntity<SucursalDTO> createSucursal(@Valid @RequestBody SucursalDTO newSucursal){
        SucursalDTO sucursalDTO = sucursalService.createSucursal(newSucursal);
        URI location = URI.create("/sucursales/"+sucursalDTO.id());
        return ResponseEntity.created(location).body(sucursalDTO);
    }

    @PutMapping("sucursales/{id}")
    public ResponseEntity<SucursalDTO> updateSucursal(@PathVariable Long id, @Valid @RequestBody SucursalDTO newSucursal){
        return ResponseEntity.ok(sucursalService.updateSucursal(id,newSucursal));
    }

    @PatchMapping("sucursales/{id}")
    public ResponseEntity<SucursalDTO> patchSucursal(@PathVariable Long id, @RequestBody Map<String, Object> bodyArray){
        return ResponseEntity.ok(sucursalService.patchSucursal(id,bodyArray));
}

    @DeleteMapping("sucursales/{id}")
    public ResponseEntity<Void> deleteSucursal(@PathVariable Long id){
        sucursalService.deleteSucursal(id);
        return ResponseEntity.noContent().build();
    }
}
