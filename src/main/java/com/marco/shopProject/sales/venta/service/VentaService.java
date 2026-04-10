package com.marco.shopProject.sales.venta.service;

import com.marco.shopProject.sales.venta.dto.CrearVentaDTO;
import com.marco.shopProject.sales.venta.dto.VentaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface VentaService {
    Page<VentaDTO> obtenerVentas(String estado, Pageable pageable);
    Page<VentaDTO> obtenerVentasPorSucursalYFecha(Long sucursalId, LocalDateTime fecha,  Pageable pageable);
    VentaDTO obtenerVentaPorId(Long id);
    VentaDTO crearVenta(Long id, CrearVentaDTO venta);
    VentaDTO eliminarVenta(Long id);
}
