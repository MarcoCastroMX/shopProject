package com.marco.shopProject.mapper;

import com.marco.shopProject.dto.*;
import com.marco.shopProject.entity.DetalleVenta;
import com.marco.shopProject.entity.Producto;
import com.marco.shopProject.entity.Sucursal;
import com.marco.shopProject.entity.Venta;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public static ProductoInventarioDTO toDTO(Producto producto){
        return ProductoInventarioDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .precio(producto.getPrecio())
                .categoria(producto.getCategoria())
                .cantidad(producto.getCantidad())
                .build();
    }

    public static MostrarProductoDTO productoTOmostrarProductoDTO(Producto producto){
        return MostrarProductoDTO.builder()
                .nombre(producto.getNombre())
                .precio(producto.getPrecio())
                .build();
    }

    public static Producto toDTO(ProductoInventarioDTO productoDTO){
        return new Producto(productoDTO.id(),productoDTO.nombre(),productoDTO.precio(), productoDTO.categoria(),productoDTO.cantidad());
    }

    public static SucursalDTO toDTO(Sucursal sucursal){
        return SucursalDTO.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .build();
    }

    public static Sucursal toDTO(SucursalDTO sucursalDTO){
        return new Sucursal(sucursalDTO.id(),sucursalDTO.nombre(),sucursalDTO.direccion());
    }

    public static VentaDTO toDTO(Venta venta){
        return VentaDTO.builder()
                .id(venta.getId())
                .fecha(venta.getFecha())
                .estado(String.valueOf(venta.getEstado()))
                .sucursalId(venta.getSucursal().getId())
                .detalle(venta.getDetalleVentas().stream()
                        .map(Mapper::toDTO)
                        .toList())
                .total(venta.getTotal())
                .build();
    }

    public static DetalleVentaDTO toDTO(DetalleVenta detalleVenta){
        return DetalleVentaDTO.builder()
                .productoId(Math.toIntExact(detalleVenta.getId()))
                .cantidad(detalleVenta.getCantidad())
                .precioUnitario(detalleVenta.getPrecioUnitario())
                .productoDTO(productoTOmostrarProductoDTO(detalleVenta.getProducto()))
                .subTotal(detalleVenta.getSubtotal())
                .build();
    }
}
