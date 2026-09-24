package com.marco.shopProject.sales.venta.service;

import com.marco.shopProject.identity.user.exception.EstadoInvalidoException;
import com.marco.shopProject.sales.detalleVenta.dto.CrearDetalleVentaDTO;
import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.catalog.sucursal.exception.SucursalNoEncontradaException;
import com.marco.shopProject.sales.venta.dto.CrearVentaDTO;
import com.marco.shopProject.sales.venta.dto.VentaDTO;
import com.marco.shopProject.sales.detalleVenta.entity.DetalleVenta;
import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import com.marco.shopProject.sales.venta.entity.Venta;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.mapper.Mapper;
import com.marco.shopProject.catalog.producto.repository.ProductoRepository;
import com.marco.shopProject.catalog.sucursal.repository.SucursalRepository;
import com.marco.shopProject.sales.venta.exception.CantidadExcedenteException;
import com.marco.shopProject.sales.venta.exception.FechaInvalidaException;
import com.marco.shopProject.sales.venta.exception.VentaNoEncontradaException;
import com.marco.shopProject.sales.venta.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    public Page<VentaDTO> obtenerVentas(String estado, Pageable pageable) {
        Optional<EstadoEnum> estadoEnum = convertirEstado(estado);

        if(estadoEnum.isPresent()){
            return ventaRepository.findAllByEstado(estadoEnum.get(), pageable)
                    .map(Mapper::toDTO);
        }

        return ventaRepository.findAll(pageable)
                    .map(Mapper::toDTO);
    }

    @Override
    public Page<VentaDTO> obtenerVentasPorSucursalYFecha(Long sucursalId, LocalDateTime fecha, Pageable pageable) {
        //Revisar si sucursal existe
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new SucursalNoEncontradaException(sucursalId));

        //Revisar si la fecha es valida
        if(fecha.isAfter(LocalDateTime.now())){
            throw new FechaInvalidaException(fecha);
        }

        LocalDateTime inicio = fecha.toLocalDate().atStartOfDay();
        LocalDateTime fin = fecha.toLocalDate().atTime(LocalTime.MAX);

        Page<VentaDTO> lista = ventaRepository.findAllBySucursalIdAndFechaBetween(sucursalId,inicio,fin,pageable)
                .map(Mapper::toDTO);

        return lista;
    }

    @Override
    public VentaDTO obtenerVentaPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNoEncontradaException(id));
        return Mapper.toDTO(venta);
    }

    @Override
    @Transactional
    public VentaDTO crearVenta(CrearVentaDTO ventaRecibida) {
        Sucursal sucursal = sucursalRepository.findById(ventaRecibida.sucursalId())
                .orElseThrow(() -> new SucursalNoEncontradaException((ventaRecibida.sucursalId())));

        Venta venta = new Venta();
        venta.setSucursal(sucursal);

        List<DetalleVenta> detalles = new ArrayList<>();
        Double totalCalculado = 0.0;

        for(CrearDetalleVentaDTO v : ventaRecibida.detalle()){
            Producto producto = productoRepository
                    .findByIdAndEstado(Long.valueOf(v.productoId()), EstadoEnum.ACTIVO)
                    .orElseThrow(()-> new ProductoNoEncontradoException(v.productoId()));

            if(producto.getCantidad() == 0 || producto.getCantidad() < v.cantidad()){
                throw new CantidadExcedenteException("Cantidad Mayor a Producto Disponible");
            }

            producto.setCantidad(producto.getCantidad()-v.cantidad());

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
    @Transactional
    public VentaDTO eliminarVenta(Long id) {
        //Buscar si existe la venta
        Venta venta = ventaRepository.findById(id).
                orElseThrow(() -> new VentaNoEncontradaException(id));

        //La baja logica es idempotente: si ya fue eliminada no vuelve a guardarse
        if(venta.getEstado() == EstadoEnum.ELIMINADO){
            return Mapper.toDTO(venta);
        }

        //Cambiar estado de la venta
        venta.setEstado(EstadoEnum.ELIMINADO);

        //Cambiar venta del Detalle venta
        for(DetalleVenta detalle : venta.getDetalleVentas()){
            detalle.setVenta(venta);
        }

        //Guardar cambios
        venta = ventaRepository.save(venta);

        return Mapper.toDTO(venta);
    }

    private Optional<EstadoEnum> convertirEstado(String estado) {
        if(estado == null || estado.isBlank()){
            return Optional.empty();
        }

        try{
            return Optional.of(EstadoEnum.valueOf(estado.trim().toUpperCase(Locale.ROOT)));
        }catch(IllegalArgumentException exception){
            throw new EstadoInvalidoException(estado);
        }
    }
}
