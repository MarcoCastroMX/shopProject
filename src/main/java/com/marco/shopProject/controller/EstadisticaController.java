package com.marco.shopProject.controller;

import com.marco.shopProject.dto.ProductoMasVendidoDTO;
import com.marco.shopProject.service.EstadisticaService;
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
    public ProductoMasVendidoDTO obtenerProductoMasVendido(){
        return estadisticaService.obtenerProductoMasVendido();
    }
}
