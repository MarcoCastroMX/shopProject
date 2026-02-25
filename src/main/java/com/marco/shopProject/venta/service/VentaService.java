package com.marco.shopProject.venta.service;

import com.marco.shopProject.venta.dto.CrearVentaDTO;
import com.marco.shopProject.venta.dto.VentaDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    List<VentaDTO> obtenerVentas(String estado);
    List<VentaDTO> obtenerVentasPorSucursalYFecha(Long sucursalId, LocalDateTime fecha);
    VentaDTO obtenerVentaPorId(Long id);
    VentaDTO crearVenta(Long id, CrearVentaDTO venta);
    VentaDTO eliminarVenta(Long id);
}
