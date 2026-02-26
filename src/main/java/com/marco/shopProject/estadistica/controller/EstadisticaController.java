package com.marco.shopProject.estadistica.controller;

import com.marco.shopProject.estadistica.dto.ProductoMasVendidoDTO;
import com.marco.shopProject.estadistica.service.EstadisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadistica")
public class EstadisticaController {

    private EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService){
        this.estadisticaService = estadisticaService;
    }

    @GetMapping("producto-mas-vendido")
    public ResponseEntity<ProductoMasVendidoDTO> obtenerProductoMasVendido(){
        return ResponseEntity.ok(estadisticaService.obtenerProductoMasVendido());
    }
}
