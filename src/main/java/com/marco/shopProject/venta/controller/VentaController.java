package com.marco.shopProject.venta.controller;

import com.marco.shopProject.venta.dto.CrearVentaDTO;
import com.marco.shopProject.venta.dto.VentaDTO;
import com.marco.shopProject.venta.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VentaController {

    private VentaService ventaService;

    @Autowired
    public VentaController(VentaService ventaService){
        this.ventaService = ventaService;
    }

    @GetMapping("/ventas")
    public List<VentaDTO> obtenerVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false) Long sucursalId
    ){
        LocalDateTime fechaConHora = (fecha != null) ? fecha.atStartOfDay() : null;

        if(sucursalId != null && fecha != null){
            List<VentaDTO> ventaDTOList = ventaService.obtenerVentasPorSucursalYFecha(sucursalId,fechaConHora);
            return ventaDTOList;
        }else{
            List<VentaDTO> ventaDTOList = ventaService.obtenerVentas(estado);
            return ventaDTOList;
        }
    }

    @GetMapping("ventas/{id}")
    public VentaDTO obtenerVentaPorId(@PathVariable Long id){
        VentaDTO ventaDTO = ventaService.obtenerVentaPorId(id);
        return ventaDTO;
    }

    @PostMapping("/ventas/{id}")
    public VentaDTO createVenta(@PathVariable Long id, @Valid @RequestBody CrearVentaDTO crearVentaDTO){
        VentaDTO venta = ventaService.crearVenta(id,crearVentaDTO);
        return venta;
    }

    @DeleteMapping("/ventas/{id}")
    public String eliminarVenta(@PathVariable Long id){
        VentaDTO ventaDTO = ventaService.eliminarVenta(id);
        return (ventaDTO.estado().equals("ELIMINADO") ? "Venta \"eliminada\" correctamente" : "Error al eliminar venta");
    }

}
