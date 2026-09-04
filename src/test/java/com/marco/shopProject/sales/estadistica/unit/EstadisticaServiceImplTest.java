package com.marco.shopProject.sales.estadistica.unit;

import com.marco.shopProject.sales.detalleVenta.repository.DetalleVentaRepository;
import com.marco.shopProject.sales.estadistica.dto.ProductoMasVendidoDTO;
import com.marco.shopProject.sales.estadistica.service.EstadisticaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private EstadisticaServiceImpl estadisticaService;

    @Test
    void obtenerProductoMasVendido_cuandoExistenProductos_retornaProductoConMayorCantidad() {
        // Arrange
        List<Object[]> ventasPorProducto = List.of(
                new Object[]{1L, "Laptop", 5L},
                new Object[]{2L, "Monitor", 12L},
                new Object[]{3L, "Mouse", 7L}
        );
        ProductoMasVendidoDTO esperado = new ProductoMasVendidoDTO(
                2L,
                "Monitor",
                12
        );

        when(detalleVentaRepository.ventasPorProducto())
                .thenReturn(ventasPorProducto);

        // Act
        ProductoMasVendidoDTO resultado =
                estadisticaService.obtenerProductoMasVendido();

        // Assert
        assertEquals(esperado, resultado);
        verify(detalleVentaRepository).ventasPorProducto();
    }

    @Test
    void obtenerProductoMasVendido_cuandoNoExistenProductos_retornaNull() {
        // Arrange
        List<Object[]> ventasPorProducto = List.of();

        when(detalleVentaRepository.ventasPorProducto())
                .thenReturn(ventasPorProducto);

        // Act
        ProductoMasVendidoDTO resultado =
                estadisticaService.obtenerProductoMasVendido();

        // Assert
        assertNull(resultado);
        verify(detalleVentaRepository).ventasPorProducto();
    }
}
