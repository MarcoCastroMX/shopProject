package com.marco.shopProject.service;

import com.marco.shopProject.dto.CrearDetalleVentaDTO;
import com.marco.shopProject.dto.CrearVentaDTO;
import com.marco.shopProject.dto.VentaDTO;
import com.marco.shopProject.entity.DetalleVenta;
import com.marco.shopProject.entity.Producto;
import com.marco.shopProject.entity.Sucursal;
import com.marco.shopProject.entity.Venta;
import com.marco.shopProject.enums.EstadoVenta;
import com.marco.shopProject.mapper.Mapper;
import com.marco.shopProject.repository.ProductoRepository;
import com.marco.shopProject.repository.SucursalRepository;
import com.marco.shopProject.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService{

    private VentaRepository ventaRepository;
    private SucursalRepository sucursalRepository;
    private ProductoRepository productoRepository;

    @Autowired
    public VentaServiceImpl(VentaRepository ventaRepository,SucursalRepository sucursalRepository,ProductoRepository productoRepository){
        this.ventaRepository = ventaRepository;
        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<VentaDTO> obtenerVentas(String estado) {
        if(estado!=null){
            if(estado.equals("ACTIVO")){
                return ventaRepository.findAllByEstado(EstadoVenta.ACTIVO).stream()
                        .map(Mapper::toDTO)
                        .toList();
            }
            return ventaRepository.findAllByEstado(EstadoVenta.ELIMINADO).stream()
                    .map(Mapper::toDTO)
                    .toList();
        }
        return ventaRepository.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @Override
    public List<VentaDTO> obtenerVentasPorSucursalYFecha(Long sucursalId, LocalDateTime fecha) {
        //Revisar si sucursal existe
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new RuntimeException("Sucursal No Existe"));

        //Revisar si la fecha es valida
        if(fecha.isAfter(LocalDateTime.now())){
            throw new RuntimeException("Fecha Invalida");
        }

        LocalDateTime inicio = fecha.toLocalDate().atStartOfDay();
        LocalDateTime fin = fecha.toLocalDate().atTime(LocalTime.MAX);

        List<VentaDTO> lista = ventaRepository.findAllBySucursalIdAndFechaBetween(sucursalId,inicio,fin).stream()
                .map(Mapper::toDTO)
                .toList();

        return lista;
    }

    @Override
    public VentaDTO obtenerVentaPorId(Long id) {
        Venta venta = ventaRepository.findById(id).orElseThrow(()-> new RuntimeException("Venta no Encontrada"));
        return Mapper.toDTO(venta);
    }

    @Override
    public VentaDTO crearVenta(Long id, CrearVentaDTO ventaRecibida) {
        if(ventaRecibida == null) throw new RuntimeException("VentaDTO es null");
        if(ventaRecibida.sucursalId() == null) throw new RuntimeException("Debe indicar la sucursal");
        if(ventaRecibida.detalle() == null || ventaRecibida.detalle().isEmpty())
            throw new RuntimeException("Debe incluir al menos un producto");

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no Encontrada"));

        Venta venta = new Venta();
        venta.setEstado(EstadoVenta.valueOf(ventaRecibida.estado()));
        venta.setSucursal(sucursal);

        List<DetalleVenta> detalles = new ArrayList<>();
        Double totalCalculado = 0.0;

        for(CrearDetalleVentaDTO v : ventaRecibida.detalle()){
            Producto producto = productoRepository.findById(v.productoId()).orElseThrow(()-> new RuntimeException("Producto no encontrado"));

            DetalleVenta detalleVenta = new DetalleVenta();

            detalleVenta.setProducto(producto);
            detalleVenta.setPrecioUnitario(producto.getPrecio());
            detalleVenta.setCantidad(v.cantidad());
            detalleVenta.setSubtotal(producto.getPrecio() * v.cantidad());
            detalleVenta.setVenta(venta);

            detalles.add(detalleVenta);
            totalCalculado += detalleVenta.getPrecioUnitario() * detalleVenta.getCantidad();
        }

        venta.setDetalleVentas(detalles);
        venta.setTotal(totalCalculado);

        venta = ventaRepository.save(venta);

        return Mapper.toDTO(venta);
    }

    @Override
    public VentaDTO eliminarVenta(Long id) {
        //Buscar si existe la venta
        Venta venta = ventaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        //Cambiar estado de la venta
        venta.setEstado(EstadoVenta.ELIMINADO);

        //Cambiar venta del Detalle venta
        for(DetalleVenta detalle : venta.getDetalleVentas()){
            detalle.setVenta(venta);
        }

        //Guardar cambios
        venta = ventaRepository.save(venta);

        return Mapper.toDTO(venta);
    }
}
