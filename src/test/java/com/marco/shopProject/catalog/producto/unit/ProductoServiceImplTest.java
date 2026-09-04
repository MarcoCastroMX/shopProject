package com.marco.shopProject.catalog.producto.unit;

import com.marco.shopProject.catalog.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.catalog.producto.repository.ProductoRepository;
import com.marco.shopProject.catalog.producto.service.ProductoServiceImpl;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Captor
    private ArgumentCaptor<Producto> productoCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> bodyCaptor;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void getAllProducts_cuandoExistenProductos_retornaPaginaDeProductosDTO() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        Producto producto1 = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        Producto producto2 = crearProducto(
                2L, "Monitor", "Tecnologia", 4000.0, 8
        );

        Page<Producto> paginaRepositorio = new PageImpl<>(
                List.of(producto1, producto2),
                pageable,
                2
        );

        List<ProductoInventarioDTO> contenidoEsperado = List.of(
                crearDTO(1L, "Laptop", "Tecnologia", 15000.0, 5),
                crearDTO(2L, "Monitor", "Tecnologia", 4000.0, 8)
        );

        when(productoRepository.findAllByEstado(EstadoEnum.ACTIVO, pageable))
                .thenReturn(paginaRepositorio);

        // Act
        Page<ProductoInventarioDTO> resultado =
                productoService.getAllProducts(EstadoEnum.ACTIVO, pageable);

        // Assert
        assertEquals(contenidoEsperado, resultado.getContent());
        assertEquals(2, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(0, resultado.getNumber());
        assertEquals(20, resultado.getSize());
        assertEquals(pageable, resultado.getPageable());
        assertFalse(resultado.hasNext());
        assertFalse(resultado.hasPrevious());

        verify(productoRepository).findAllByEstado(EstadoEnum.ACTIVO, pageable);
    }

    @Test
    void getAllProducts_cuandoNoExistenProductos_retornaPaginaVacia() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Producto> paginaVacia = Page.empty(pageable);

        when(productoRepository.findAllByEstado(EstadoEnum.ELIMINADO, pageable))
                .thenReturn(paginaVacia);

        // Act
        Page<ProductoInventarioDTO> resultado =
                productoService.getAllProducts(EstadoEnum.ELIMINADO, pageable);

        // Assert
        assertTrue(resultado.isEmpty());
        assertTrue(resultado.getContent().isEmpty());
        assertEquals(0, resultado.getNumberOfElements());
        assertEquals(0, resultado.getTotalElements());
        assertEquals(0, resultado.getTotalPages());
        assertEquals(pageable, resultado.getPageable());
        assertFalse(resultado.hasNext());
        assertFalse(resultado.hasPrevious());

        verify(productoRepository).findAllByEstado(EstadoEnum.ELIMINADO, pageable);
    }

    @Test
    void obtenerProductoPorId_cuandoProductoExiste_retornaProductoDTO() {
        // Arrange
        int id = 1;
        Producto producto = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(producto));

        // Act
        ProductoInventarioDTO resultado =
                productoService.obtenerProductoPorId(id);

        // Assert
        assertEquals(esperado, resultado);
        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
    }

    @Test
    void obtenerProductoPorId_cuandoProductoNoExiste_lanzaProductoNoEncontradoException() {
        // Arrange
        int id = 1;

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.obtenerProductoPorId(id)
        );

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
    }

    @Test
    void createProduct_cuandoRecibeProductoDTO_guardaProductoYRetornaDTO() {
        // Arrange
        ProductoInventarioDTO recibido = crearDTO(
                null, "Laptop", "Tecnologia", 15000.0, 5
        );
        Producto productoGuardado = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(productoGuardado);

        // Act
        ProductoInventarioDTO resultado =
                productoService.createProduct(recibido);

        // Assert
        assertEquals(esperado, resultado);

        verify(productoRepository).save(productoCaptor.capture());

        Producto productoEnviado = productoCaptor.getValue();

        assertNull(productoEnviado.getId());
        assertEquals(EstadoEnum.ACTIVO, productoEnviado.getEstado());
        verificarCampos(recibido, productoEnviado);
    }

    @Test
    void updateProduct_cuandoProductoExiste_actualizaProductoYRetornaDTO() {
        // Arrange
        int id = 1;

        Producto productoExistente = crearProducto(
                1L, "Nombre anterior", "Categoria anterior", 100.0, 1
        );
        ProductoInventarioDTO recibido = crearDTO(
                null, "Nombre nuevo", "Categoria nueva", 250.0, 4
        );
        Producto productoGuardado = crearProducto(
                1L, "Nombre nuevo", "Categoria nueva", 250.0, 4
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Nombre nuevo", "Categoria nueva", 250.0, 4
        );

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class)))
                .thenReturn(productoGuardado);

        // Act
        ProductoInventarioDTO resultado =
                productoService.updateProduct(id, recibido);

        // Assert
        assertEquals(esperado, resultado);

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(productoRepository).save(productoCaptor.capture());

        Producto productoEnviado = productoCaptor.getValue();

        assertEquals(1L, productoEnviado.getId());
        verificarCampos(recibido, productoEnviado);
    }

    @Test
    void updateProduct_cuandoProductoNoExiste_lanzaProductoNoEncontradoException() {
        // Arrange
        int id = 1;
        ProductoInventarioDTO recibido = crearDTO(
                null, "Nombre nuevo", "Categoria nueva", 250.0, 4
        );

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.updateProduct(id, recibido)
        );

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void partialUpdateProduct_cuandoProductoExiste_guardaCambiosYRetornaDTO() {
        // Arrange
        int id = 1;

        Producto productoExistente = crearProducto(
                1L, "Nombre anterior", "Tecnologia", 100.0, 2
        );

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Nombre actualizado");
        body.put("precio", 200.0);

        Producto productoActualizado = crearProducto(
                1L, "Nombre actualizado", "Tecnologia", 200.0, 2
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Nombre actualizado", "Tecnologia", 200.0, 2
        );

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(productoExistente));
        when(jsonMapper.updateValue(productoExistente, body))
                .thenReturn(productoActualizado);
        when(productoRepository.save(productoActualizado))
                .thenReturn(productoActualizado);

        // Act
        ProductoInventarioDTO resultado =
                productoService.partialUpdateProduct(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(jsonMapper).updateValue(productoExistente, body);
        verify(productoRepository).save(productoActualizado);
    }

    @Test
    void partialUpdateProduct_cuandoBodyIncluyeId_ignoraIdGuardaCambiosYRetornaDTO() {
        // Arrange
        int id = 1;

        Producto productoExistente = crearProducto(
                1L, "Nombre anterior", "Tecnologia", 100.0, 2
        );

        Map<String, Object> body = new HashMap<>();
        body.put("id", 999L);
        body.put("estado", "ELIMINADO");
        body.put("nombre", "Nombre protegido");

        Producto productoActualizado = crearProducto(
                1L, "Nombre protegido", "Tecnologia", 100.0, 2
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Nombre protegido", "Tecnologia", 100.0, 2
        );

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.of(productoExistente));
        when(jsonMapper.updateValue(eq(productoExistente), anyMap()))
                .thenReturn(productoActualizado);
        when(productoRepository.save(productoActualizado))
                .thenReturn(productoActualizado);

        // Act
        ProductoInventarioDTO resultado =
                productoService.partialUpdateProduct(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verify(jsonMapper).updateValue(
                eq(productoExistente),
                bodyCaptor.capture()
        );
        verify(productoRepository).save(productoActualizado);

        Map<String, Object> bodyEnviado = bodyCaptor.getValue();

        assertFalse(bodyEnviado.containsKey("id"));
        assertFalse(bodyEnviado.containsKey("estado"));
        assertEquals("Nombre protegido", bodyEnviado.get("nombre"));
    }

    @Test
    void partialUpdateProduct_cuandoProductoNoExiste_lanzaProductoNoEncontradoException() {
        // Arrange
        int id = 1;

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Nombre actualizado");

        when(productoRepository.findByIdAndEstado(1L, EstadoEnum.ACTIVO))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.partialUpdateProduct(id, body)
        );

        verify(productoRepository).findByIdAndEstado(1L, EstadoEnum.ACTIVO);
        verifyNoInteractions(jsonMapper);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void deleteProduct_cuandoProductoEstaActivo_loMarcaComoEliminado() {
        // Arrange
        int id = 1;
        Producto producto = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act
        productoService.deleteProduct(id);

        // Assert
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(productoCaptor.capture());

        assertEquals(EstadoEnum.ELIMINADO, productoCaptor.getValue().getEstado());
    }

    @Test
    void deleteProduct_cuandoProductoYaEstaEliminado_mantieneOperacionIdempotente() {
        // Arrange
        int id = 1;
        Producto producto = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        producto.setEstado(EstadoEnum.ELIMINADO);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act
        productoService.deleteProduct(id);

        // Assert
        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
        assertEquals(EstadoEnum.ELIMINADO, producto.getEstado());
    }

    @Test
    void deleteProduct_cuandoProductoNoExiste_lanzaProductoNoEncontradoException() {
        // Arrange
        int id = 1;

        when(productoRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.deleteProduct(id)
        );

        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void restoreProduct_cuandoProductoEstaEliminado_loActivaYRetornaDTO() {
        // Arrange
        int id = 1;
        Producto producto = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        producto.setEstado(EstadoEnum.ELIMINADO);
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(productoRepository.save(producto))
                .thenReturn(producto);

        // Act
        ProductoInventarioDTO resultado = productoService.restoreProduct(id);

        // Assert
        assertEquals(esperado, resultado);
        assertEquals(EstadoEnum.ACTIVO, producto.getEstado());
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(producto);
    }

    @Test
    void restoreProduct_cuandoProductoYaEstaActivo_retornaDTOSinGuardar() {
        // Arrange
        int id = 1;
        Producto producto = crearProducto(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );
        ProductoInventarioDTO esperado = crearDTO(
                1L, "Laptop", "Tecnologia", 15000.0, 5
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act
        ProductoInventarioDTO resultado = productoService.restoreProduct(id);

        // Assert
        assertEquals(esperado, resultado);
        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void restoreProduct_cuandoProductoNoExiste_lanzaProductoNoEncontradoException() {
        // Arrange
        int id = 1;

        when(productoRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.restoreProduct(id)
        );

        verify(productoRepository).findById(1L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    private Producto crearProducto(
            Long id,
            String nombre,
            String categoria,
            Double precio,
            int cantidad
    ) {
        return Producto.builder()
                .id(id)
                .nombre(nombre)
                .categoria(categoria)
                .precio(precio)
                .cantidad(cantidad)
                .build();
    }

    private ProductoInventarioDTO crearDTO(
            Long id,
            String nombre,
            String categoria,
            Double precio,
            int cantidad
    ) {
        return new ProductoInventarioDTO(
                id,
                nombre,
                categoria,
                precio,
                cantidad
        );
    }

    private void verificarCampos(
            ProductoInventarioDTO esperado,
            Producto actual
    ) {
        assertEquals(esperado.nombre(), actual.getNombre());
        assertEquals(esperado.categoria(), actual.getCategoria());
        assertEquals(esperado.precio(), actual.getPrecio());
        assertEquals(esperado.cantidad(), actual.getCantidad());
    }
}
