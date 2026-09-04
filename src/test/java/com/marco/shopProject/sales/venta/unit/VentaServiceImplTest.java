package com.marco.shopProject.sales.venta.unit;

import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.catalog.producto.repository.ProductoRepository;
import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import com.marco.shopProject.catalog.sucursal.repository.SucursalRepository;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.sales.detalleVenta.dto.CrearDetalleVentaDTO;
import com.marco.shopProject.sales.venta.dto.CrearVentaDTO;
import com.marco.shopProject.sales.venta.entity.Venta;
import com.marco.shopProject.sales.venta.repository.VentaRepository;
import com.marco.shopProject.sales.venta.service.VentaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void crearVenta_cuandoProductoEstaEliminado_lanzaProductoNoEncontradoException() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = Sucursal.builder()
                .id(sucursalId)
                .nombre("Sucursal Centro")
                .direccion("Avenida Principal 100")
                .build();
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                EstadoEnum.ACTIVO.name(),
                1,
                List.of(new CrearDetalleVentaDTO(1, 1))
        );

        when(sucursalRepository.findById(sucursalId))
                .thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> ventaService.crearVenta(sucursalId, ventaRecibida)
        );

        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(ventaRepository, never()).save(any(Venta.class));
        verify(productoRepository, never()).save(any(Producto.class));
    }
}
