package com.marco.shopProject.venta.controller;

import com.marco.shopProject.venta.dto.CrearVentaDTO;
import com.marco.shopProject.venta.dto.VentaDTO;
import com.marco.shopProject.venta.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VentaController {

    private final VentaService ventaService;

    @Autowired
    public VentaController(VentaService ventaService){
        this.ventaService = ventaService;
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<VentaDTO>> obtenerVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false) Long sucursalId
    ){
        LocalDateTime fechaConHora = (fecha != null) ? fecha.atStartOfDay() : null;

        if(sucursalId != null && fecha != null){
            List<VentaDTO> ventaDTOList = ventaService.obtenerVentasPorSucursalYFecha(sucursalId,fechaConHora);
            return ResponseEntity.ok(ventaDTOList);
        }else{
            List<VentaDTO> ventaDTOList = ventaService.obtenerVentas(estado);
            return ResponseEntity.ok(ventaDTOList);
        }
    }

    @GetMapping("ventas/{id}")
    public ResponseEntity<VentaDTO> obtenerVentaPorId(@PathVariable Long id){
        VentaDTO ventaDTO = ventaService.obtenerVentaPorId(id);
        return ResponseEntity.ok(ventaDTO);
    }

    @PostMapping("/ventas/{id}")
    public ResponseEntity<VentaDTO> createVenta(@PathVariable Long id, @Valid @RequestBody CrearVentaDTO crearVentaDTO){
        VentaDTO venta = ventaService.crearVenta(id,crearVentaDTO);

        URI location = URI.create("/ventas/"+venta.id());
        return ResponseEntity.created(location).body(venta);
    }

    @DeleteMapping("/ventas/{id}")
    public ResponseEntity<Map<String,String>> eliminarVenta(@PathVariable Long id){
        VentaDTO ventaDTO = ventaService.eliminarVenta(id);

        return ResponseEntity.ok((ventaDTO.estado().equals("ELIMINADO") ? Map.of("message","Venta \"eliminada\" correctamente") : Map.of("message","Error al eliminar venta")));
    }

}
