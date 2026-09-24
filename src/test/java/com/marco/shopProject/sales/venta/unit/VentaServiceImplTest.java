package com.marco.shopProject.sales.venta.unit;

import com.marco.shopProject.catalog.producto.dto.MostrarProductoDTO;
import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.catalog.producto.repository.ProductoRepository;
import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import com.marco.shopProject.catalog.sucursal.exception.SucursalNoEncontradaException;
import com.marco.shopProject.catalog.sucursal.repository.SucursalRepository;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.identity.user.exception.EstadoInvalidoException;
import com.marco.shopProject.sales.detalleVenta.dto.CrearDetalleVentaDTO;
import com.marco.shopProject.sales.detalleVenta.dto.DetalleVentaDTO;
import com.marco.shopProject.sales.detalleVenta.entity.DetalleVenta;
import com.marco.shopProject.sales.venta.dto.CrearVentaDTO;
import com.marco.shopProject.sales.venta.dto.VentaDTO;
import com.marco.shopProject.sales.venta.entity.Venta;
import com.marco.shopProject.sales.venta.exception.CantidadExcedenteException;
import com.marco.shopProject.sales.venta.exception.FechaInvalidaException;
import com.marco.shopProject.sales.venta.exception.VentaNoEncontradaException;
import com.marco.shopProject.sales.venta.repository.VentaRepository;
import com.marco.shopProject.sales.venta.service.VentaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Captor
    private ArgumentCaptor<Venta> ventaCaptor;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void obtenerVentas_cuandoEstadoEsActivoYExistenVentas_retornaPaginaDeVentasDTO() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2);
        Sucursal sucursal = crearSucursal(1L);
        Producto producto1 = crearProducto(1L, "Laptop", 1000.0, 5);
        Producto producto2 = crearProducto(2L, "Monitor", 500.0, 8);
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 9, 12, 0);
        Venta venta1 = crearVenta(1L, fecha, EstadoEnum.ACTIVO, sucursal,
                List.of(producto1), List.of(1));
        Venta venta2 = crearVenta(2L, fecha.plusHours(1), EstadoEnum.ACTIVO, sucursal,
                List.of(producto2), List.of(2));
        Page<Venta> paginaRepositorio = new PageImpl<>(List.of(venta1, venta2), pageable, 6);
        Page<VentaDTO> paginaEsperada = new PageImpl<>(
                List.of(crearDTO(venta1), crearDTO(venta2)), pageable, 6);

        when(ventaRepository.findAllByEstado(EstadoEnum.ACTIVO, pageable))
                .thenReturn(paginaRepositorio);

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentas("ACTIVO", pageable);

        // Assert
        verificarPagina(paginaEsperada, resultado);
        verify(ventaRepository).findAllByEstado(EstadoEnum.ACTIVO, pageable);
        verify(ventaRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void obtenerVentas_cuandoEstadoEsActivoYNoExistenVentas_retornaPaginaVacia() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(ventaRepository.findAllByEstado(EstadoEnum.ACTIVO, pageable))
                .thenReturn(Page.empty(pageable));

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentas("ACTIVO", pageable);

        // Assert
        verificarPagina(Page.empty(pageable), resultado);
        verify(ventaRepository).findAllByEstado(EstadoEnum.ACTIVO, pageable);
        verify(ventaRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void obtenerVentas_cuandoEstadoEsNuloYExistenVentas_retornaPaginaSinFiltro() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Sucursal sucursal = crearSucursal(1L);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        Venta venta = crearVenta(1L, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ACTIVO, sucursal, List.of(producto), List.of(1));
        Page<Venta> paginaRepositorio = new PageImpl<>(List.of(venta), pageable, 1);
        Page<VentaDTO> paginaEsperada = new PageImpl<>(List.of(crearDTO(venta)), pageable, 1);

        when(ventaRepository.findAll(pageable)).thenReturn(paginaRepositorio);

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentas(null, pageable);

        // Assert
        verificarPagina(paginaEsperada, resultado);
        verify(ventaRepository).findAll(pageable);
        verify(ventaRepository, never())
                .findAllByEstado(any(EstadoEnum.class), any(Pageable.class));
    }

    @Test
    void obtenerVentas_cuandoEstadoEstaEnBlancoYNoExistenVentas_retornaPaginaVacia() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(ventaRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentas("  ", pageable);

        // Assert
        verificarPagina(Page.empty(pageable), resultado);
        verify(ventaRepository).findAll(pageable);
        verify(ventaRepository, never())
                .findAllByEstado(any(EstadoEnum.class), any(Pageable.class));
    }

    @Test
    void obtenerVentas_cuandoEstadoTieneEspaciosYMinusculas_normalizaYRetornaPagina() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Sucursal sucursal = crearSucursal(1L);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        Venta venta = crearVenta(1L, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ELIMINADO, sucursal, List.of(producto), List.of(1));
        Page<Venta> paginaRepositorio = new PageImpl<>(List.of(venta), pageable, 1);
        Page<VentaDTO> paginaEsperada = new PageImpl<>(List.of(crearDTO(venta)), pageable, 1);

        when(ventaRepository.findAllByEstado(EstadoEnum.ELIMINADO, pageable))
                .thenReturn(paginaRepositorio);

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentas(" eliminado ", pageable);

        // Assert
        verificarPagina(paginaEsperada, resultado);
        verify(ventaRepository).findAllByEstado(EstadoEnum.ELIMINADO, pageable);
        verify(ventaRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void obtenerVentas_cuandoEstadoEsInvalido_lanzaEstadoInvalidoException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(EstadoInvalidoException.class,
                () -> ventaService.obtenerVentas("EN PROCESO", pageable));
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void obtenerVentasPorSucursalYFecha_cuandoSucursalYFechaSonValidas_retornaPaginaDeVentasDTO() {
        // Arrange
        long sucursalId = 1L;
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 9, 12, 0);
        Pageable pageable = PageRequest.of(0, 20);
        Sucursal sucursal = crearSucursal(sucursalId);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        Venta venta = crearVenta(1L, fecha, EstadoEnum.ACTIVO, sucursal,
                List.of(producto), List.of(1));
        Page<Venta> paginaRepositorio = new PageImpl<>(List.of(venta), pageable, 1);
        Page<VentaDTO> paginaEsperada = new PageImpl<>(List.of(crearDTO(venta)), pageable, 1);
        LocalDateTime inicio = fecha.toLocalDate().atStartOfDay();
        LocalDateTime fin = fecha.toLocalDate().atTime(LocalTime.MAX);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(ventaRepository.findAllBySucursalIdAndFechaBetween(
                sucursalId, inicio, fin, pageable)).thenReturn(paginaRepositorio);

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentasPorSucursalYFecha(
                sucursalId, fecha, pageable);

        // Assert
        verificarPagina(paginaEsperada, resultado);
        verify(sucursalRepository).findById(sucursalId);
        verify(ventaRepository).findAllBySucursalIdAndFechaBetween(
                sucursalId, inicio, fin, pageable);
    }

    @Test
    void obtenerVentasPorSucursalYFecha_cuandoNoExistenVentas_retornaPaginaVacia() {
        // Arrange
        long sucursalId = 1L;
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 9, 12, 0);
        Pageable pageable = PageRequest.of(0, 20);
        Sucursal sucursal = crearSucursal(sucursalId);
        LocalDateTime inicio = fecha.toLocalDate().atStartOfDay();
        LocalDateTime fin = fecha.toLocalDate().atTime(LocalTime.MAX);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(ventaRepository.findAllBySucursalIdAndFechaBetween(
                sucursalId, inicio, fin, pageable)).thenReturn(Page.empty(pageable));

        // Act
        Page<VentaDTO> resultado = ventaService.obtenerVentasPorSucursalYFecha(
                sucursalId, fecha, pageable);

        // Assert
        verificarPagina(Page.empty(pageable), resultado);
        verify(sucursalRepository).findById(sucursalId);
        verify(ventaRepository).findAllBySucursalIdAndFechaBetween(
                sucursalId, inicio, fin, pageable);
    }

    @Test
    void obtenerVentasPorSucursalYFecha_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long sucursalId = 1L;
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 9, 12, 0);
        Pageable pageable = PageRequest.of(0, 20);
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(SucursalNoEncontradaException.class,
                () -> ventaService.obtenerVentasPorSucursalYFecha(
                        sucursalId, fecha, pageable));
        verify(sucursalRepository).findById(sucursalId);
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void obtenerVentasPorSucursalYFecha_cuandoFechaEsFutura_lanzaFechaInvalidaException() {
        // Arrange
        long sucursalId = 1L;
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 20);
        Sucursal sucursal = crearSucursal(sucursalId);
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));

        // Act y Assert
        assertThrows(FechaInvalidaException.class,
                () -> ventaService.obtenerVentasPorSucursalYFecha(
                        sucursalId, fechaFutura, pageable));
        verify(sucursalRepository).findById(sucursalId);
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void obtenerVentaPorId_cuandoVentaExiste_retornaVentaDTO() {
        // Arrange
        long id = 1L;
        Sucursal sucursal = crearSucursal(1L);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        Venta venta = crearVenta(id, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ACTIVO, sucursal, List.of(producto), List.of(1));
        VentaDTO esperado = crearDTO(venta);
        when(ventaRepository.findById(id)).thenReturn(Optional.of(venta));

        // Act
        VentaDTO resultado = ventaService.obtenerVentaPorId(id);

        // Assert
        assertEquals(esperado, resultado);
        assertEquals(id, resultado.id());
        verify(ventaRepository).findById(id);
    }

    @Test
    void obtenerVentaPorId_cuandoVentaNoExiste_lanzaVentaNoEncontradaException() {
        // Arrange
        long id = 5L;
        when(ventaRepository.findById(id)).thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(VentaNoEncontradaException.class,
                () -> ventaService.obtenerVentaPorId(id));
        verify(ventaRepository).findById(id);
    }

    @Test
    void crearVenta_cuandoDatosSonValidos_guardaVentaYRetornaVentaDTO() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = crearSucursal(sucursalId);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                sucursalId, List.of(new CrearDetalleVentaDTO(1, 2)));
        Venta ventaGuardada = crearVenta(10L, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ACTIVO, sucursal, List.of(producto), List.of(2));
        VentaDTO esperado = crearDTO(ventaGuardada);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        // Act
        VentaDTO resultado = ventaService.crearVenta(ventaRecibida);

        // Assert
        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();
        DetalleVenta detalle = ventaEnviada.getDetalleVentas().getFirst();

        assertNull(ventaEnviada.getId());
        assertNull(ventaEnviada.getFecha());
        assertNull(ventaEnviada.getEstado());
        assertSame(sucursal, ventaEnviada.getSucursal());
        assertEquals(1, ventaEnviada.getDetalleVentas().size());
        assertSame(producto, detalle.getProducto());
        assertEquals(2, detalle.getCantidad());
        assertEquals(1000.0, detalle.getPrecioUnitario(), 0.001);
        assertEquals(2000.0, detalle.getSubtotal(), 0.001);
        assertEquals(2000.0, ventaEnviada.getTotal(), 0.001);
        assertSame(ventaEnviada, detalle.getVenta());
        assertEquals(3, producto.getCantidad());
        assertEquals(esperado, resultado);
    }

    @Test
    void crearVenta_cuandoTieneDosProductos_calculaDetallesYTotal() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = crearSucursal(sucursalId);
        Producto producto1 = crearProducto(1L, "Laptop", 1000.0, 3);
        Producto producto2 = crearProducto(2L, "Monitor", 500.0, 2);
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(sucursalId, List.of(
                new CrearDetalleVentaDTO(1, 2),
                new CrearDetalleVentaDTO(2, 1)));
        Venta ventaGuardada = crearVenta(10L, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ACTIVO, sucursal,
                List.of(producto1, producto2), List.of(2, 1));
        VentaDTO esperado = crearDTO(ventaGuardada);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto1));
        when(productoRepository.findByIdAndEstado(2L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        // Act
        VentaDTO resultado = ventaService.crearVenta(ventaRecibida);

        // Assert
        verify(sucursalRepository).findById(sucursalId);
        InOrder ordenProductos = inOrder(productoRepository);
        ordenProductos.verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        ordenProductos.verify(productoRepository).findByIdAndEstado(2L, EstadoEnum.ACTIVO);
        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();
        DetalleVenta detalle1 = ventaEnviada.getDetalleVentas().get(0);
        DetalleVenta detalle2 = ventaEnviada.getDetalleVentas().get(1);

        assertNull(ventaEnviada.getFecha());
        assertNull(ventaEnviada.getEstado());
        assertSame(sucursal, ventaEnviada.getSucursal());
        assertEquals(2, ventaEnviada.getDetalleVentas().size());
        assertSame(producto1, detalle1.getProducto());
        assertEquals(2, detalle1.getCantidad());
        assertEquals(1000.0, detalle1.getPrecioUnitario(), 0.001);
        assertEquals(2000.0, detalle1.getSubtotal(), 0.001);
        assertSame(ventaEnviada, detalle1.getVenta());
        assertSame(producto2, detalle2.getProducto());
        assertEquals(1, detalle2.getCantidad());
        assertEquals(500.0, detalle2.getPrecioUnitario(), 0.001);
        assertEquals(500.0, detalle2.getSubtotal(), 0.001);
        assertSame(ventaEnviada, detalle2.getVenta());
        assertEquals(1, producto1.getCantidad());
        assertEquals(1, producto2.getCantidad());
        assertEquals(2500.0, ventaEnviada.getTotal(), 0.001);
        assertEquals(esperado, resultado);
    }

    @Test
    void crearVenta_cuandoCantidadCoincideConExistencias_dejaInventarioEnCero() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = crearSucursal(sucursalId);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 2);
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                sucursalId, List.of(new CrearDetalleVentaDTO(1, 2)));
        Venta ventaGuardada = crearVenta(10L, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ACTIVO, sucursal, List.of(producto), List.of(2));
        VentaDTO esperado = crearDTO(ventaGuardada);

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        // Act
        VentaDTO resultado = ventaService.crearVenta(ventaRecibida);

        // Assert
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();
        DetalleVenta detalle = ventaEnviada.getDetalleVentas().getFirst();

        assertEquals(2, detalle.getCantidad());
        assertEquals(0, producto.getCantidad());
        assertEquals(2000.0, detalle.getSubtotal(), 0.001);
        assertEquals(2000.0, ventaEnviada.getTotal(), 0.001);
        assertSame(ventaEnviada, detalle.getVenta());
        assertEquals(esperado, resultado);
    }

    @Test
    void crearVenta_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long sucursalId = 5L;
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                sucursalId, List.of(new CrearDetalleVentaDTO(1, 1)));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(SucursalNoEncontradaException.class,
                () -> ventaService.crearVenta(ventaRecibida));
        verify(sucursalRepository).findById(sucursalId);
        verifyNoInteractions(productoRepository, ventaRepository);
    }

    @Test
    void crearVenta_cuandoProductoEstaEliminado_lanzaProductoNoEncontradoException() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = crearSucursal(sucursalId);
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                sucursalId, List.of(new CrearDetalleVentaDTO(1, 1)));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(ProductoNoEncontradoException.class,
                () -> ventaService.crearVenta(ventaRecibida));
        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void crearVenta_cuandoCantidadEsInsuficiente_lanzaCantidadExcedenteException() {
        // Arrange
        long sucursalId = 1L;
        Sucursal sucursal = crearSucursal(sucursalId);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 1);
        CrearVentaDTO ventaRecibida = new CrearVentaDTO(
                sucursalId, List.of(new CrearDetalleVentaDTO(1, 2)));
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto));

        // Act y Assert
        assertThrows(CantidadExcedenteException.class,
                () -> ventaService.crearVenta(ventaRecibida));
        verify(sucursalRepository).findById(sucursalId);
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(ventaRepository, never()).save(any(Venta.class));
        assertEquals(1, producto.getCantidad());
    }

    @Test
    void eliminarVenta_cuandoVentaExiste_cambiaEstadoAEliminado() {
        // Arrange
        long id = 1L;
        Sucursal sucursal = crearSucursal(1L);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        LocalDateTime fecha = LocalDateTime.of(2026, 9, 9, 12, 0);
        Venta venta = crearVenta(id, fecha, EstadoEnum.ACTIVO, sucursal,
                List.of(producto), List.of(1));
        Venta ventaEliminadaEsperada = crearVenta(id, fecha, EstadoEnum.ELIMINADO, sucursal,
                List.of(producto), List.of(1));
        VentaDTO esperado = crearDTO(ventaEliminadaEsperada);
        when(ventaRepository.findById(id)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(venta)).thenReturn(venta);

        // Act
        VentaDTO resultado = ventaService.eliminarVenta(id);

        // Assert
        verify(ventaRepository).findById(id);
        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();

        assertSame(venta, ventaEnviada);
        assertEquals(EstadoEnum.ELIMINADO, ventaEnviada.getEstado());
        assertTrue(ventaEnviada.getDetalleVentas().stream()
                .allMatch(detalle -> detalle.getVenta() == ventaEnviada));
        assertEquals(esperado, resultado);
    }

    @Test
    void eliminarVenta_cuandoVentaYaEstaEliminada_retornaDTOYNoVuelveAGuardar() {
        // Arrange
        long id = 1L;
        Sucursal sucursal = crearSucursal(1L);
        Producto producto = crearProducto(1L, "Laptop", 1000.0, 5);
        Venta venta = crearVenta(id, LocalDateTime.of(2026, 9, 9, 12, 0),
                EstadoEnum.ELIMINADO, sucursal, List.of(producto), List.of(1));
        VentaDTO esperado = crearDTO(venta);
        when(ventaRepository.findById(id)).thenReturn(Optional.of(venta));

        // Act
        VentaDTO resultado = ventaService.eliminarVenta(id);

        // Assert
        assertEquals(esperado, resultado);
        assertEquals("ELIMINADO", resultado.estado());
        verify(ventaRepository).findById(id);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void eliminarVenta_cuandoVentaNoExiste_lanzaVentaNoEncontradaException() {
        // Arrange
        long id = 5L;
        when(ventaRepository.findById(id)).thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(VentaNoEncontradaException.class,
                () -> ventaService.eliminarVenta(id));
        verify(ventaRepository).findById(id);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    private Sucursal crearSucursal(Long id) {
        return Sucursal.builder()
                .id(id)
                .nombre("Sucursal Centro")
                .direccion("Avenida Principal 100")
                .telefono("5555555555")
                .build();
    }

    private Producto crearProducto(Long id, String nombre, Double precio, int cantidad) {
        return Producto.builder()
                .id(id)
                .nombre(nombre)
                .precio(precio)
                .categoria("Tecnologia")
                .cantidad(cantidad)
                .estado(EstadoEnum.ACTIVO)
                .build();
    }

    private Venta crearVenta(
            Long id,
            LocalDateTime fecha,
            EstadoEnum estado,
            Sucursal sucursal,
            List<Producto> productos,
            List<Integer> cantidades
    ) {
        Venta venta = Venta.builder()
                .id(id)
                .fecha(fecha)
                .estado(estado)
                .sucursal(sucursal)
                .build();
        List<DetalleVenta> detalles = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            int cantidad = cantidades.get(i);
            double subtotal = producto.getPrecio() * cantidad;
            DetalleVenta detalle = DetalleVenta.builder()
                    .id((long) i + 1)
                    .cantidad(cantidad)
                    .precioUnitario(producto.getPrecio())
                    .venta(venta)
                    .producto(producto)
                    .subtotal(subtotal)
                    .build();
            detalles.add(detalle);
            total += subtotal;
        }

        venta.setDetalleVentas(detalles);
        venta.setTotal(total);
        return venta;
    }

    private VentaDTO crearDTO(Venta venta) {
        List<DetalleVentaDTO> detalles = venta.getDetalleVentas().stream()
                .map(detalle -> new DetalleVentaDTO(
                        Math.toIntExact(detalle.getProducto().getId()),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        new MostrarProductoDTO(
                                detalle.getProducto().getNombre(),
                                detalle.getProducto().getPrecio()),
                        detalle.getSubtotal()
                ))
                .toList();

        return new VentaDTO(
                venta.getId(),
                venta.getFecha(),
                String.valueOf(venta.getEstado()),
                venta.getSucursal().getId(),
                detalles,
                venta.getTotal()
        );
    }

    private void verificarPagina(Page<VentaDTO> esperada, Page<VentaDTO> actual) {
        assertEquals(esperada.getContent(), actual.getContent());
        assertEquals(esperada.getNumberOfElements(), actual.getNumberOfElements());
        assertEquals(esperada.getTotalElements(), actual.getTotalElements());
        assertEquals(esperada.getTotalPages(), actual.getTotalPages());
        assertEquals(esperada.getNumber(), actual.getNumber());
        assertEquals(esperada.getSize(), actual.getSize());
        assertEquals(esperada.getPageable(), actual.getPageable());
        assertEquals(esperada.hasNext(), actual.hasNext());
        assertEquals(esperada.hasPrevious(), actual.hasPrevious());
    }
}
