package com.marco.shopProject.catalog.sucursal.unit;

import com.marco.shopProject.catalog.sucursal.dto.SucursalDTO;
import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import com.marco.shopProject.catalog.sucursal.exception.SucursalNoEncontradaException;
import com.marco.shopProject.catalog.sucursal.repository.SucursalRepository;
import com.marco.shopProject.catalog.sucursal.service.SucursalServiceImpl;
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
class SucursalServiceImplTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Captor
    private ArgumentCaptor<Sucursal> sucursalCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> bodyCaptor;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    @Test
    void getAllSucursales_cuandoExistenSucursales_retornaPaginaDeSucursalesDTO() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        Sucursal sucursal1 = crearSucursal(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );
        Sucursal sucursal2 = crearSucursal(
                2L, "Sucursal Norte", "Calle Norte 200"
        );

        Page<Sucursal> paginaRepositorio = new PageImpl<>(
                List.of(sucursal1, sucursal2),
                pageable,
                2
        );

        List<SucursalDTO> contenidoEsperado = List.of(
                crearDTO(1L, "Sucursal Centro", "Avenida Principal 100"),
                crearDTO(2L, "Sucursal Norte", "Calle Norte 200")
        );

        when(sucursalRepository.findAll(pageable))
                .thenReturn(paginaRepositorio);

        // Act
        Page<SucursalDTO> resultado =
                sucursalService.getAllSucursales(pageable);

        // Assert
        assertEquals(contenidoEsperado, resultado.getContent());
        assertEquals(2, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertEquals(0, resultado.getNumber());
        assertEquals(20, resultado.getSize());
        assertEquals(pageable, resultado.getPageable());
        assertFalse(resultado.hasNext());
        assertFalse(resultado.hasPrevious());

        verify(sucursalRepository).findAll(pageable);
    }

    @Test
    void getAllSucursales_cuandoNoExistenSucursales_retornaPaginaVacia() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Sucursal> paginaVacia = Page.empty(pageable);

        when(sucursalRepository.findAll(pageable))
                .thenReturn(paginaVacia);

        // Act
        Page<SucursalDTO> resultado =
                sucursalService.getAllSucursales(pageable);

        // Assert
        assertTrue(resultado.isEmpty());
        assertTrue(resultado.getContent().isEmpty());
        assertEquals(0, resultado.getNumberOfElements());
        assertEquals(0, resultado.getTotalElements());
        assertEquals(0, resultado.getTotalPages());
        assertEquals(pageable, resultado.getPageable());
        assertFalse(resultado.hasNext());
        assertFalse(resultado.hasPrevious());

        verify(sucursalRepository).findAll(pageable);
    }

    @Test
    void getSucursalById_cuandoSucursalExiste_retornaSucursalDTO() {
        // Arrange
        long id = 1L;
        Sucursal sucursal = crearSucursal(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );
        SucursalDTO esperado = crearDTO(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.of(sucursal));

        // Act
        SucursalDTO resultado = sucursalService.getSucursalById(id);

        // Assert
        assertEquals(esperado, resultado);
        verify(sucursalRepository).findById(id);
    }

    @Test
    void getSucursalById_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long id = 1L;

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                SucursalNoEncontradaException.class,
                () -> sucursalService.getSucursalById(id)
        );

        verify(sucursalRepository).findById(id);
    }

    @Test
    void createSucursal_cuandoRecibeSucursalDTO_guardaSucursalYRetornaDTO() {
        // Arrange
        SucursalDTO recibido = crearDTO(
                null, "Sucursal Centro", "Avenida Principal 100"
        );
        Sucursal sucursalGuardada = crearSucursal(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );
        SucursalDTO esperado = crearDTO(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );

        when(sucursalRepository.save(any(Sucursal.class)))
                .thenReturn(sucursalGuardada);

        // Act
        SucursalDTO resultado = sucursalService.createSucursal(recibido);

        // Assert
        assertEquals(esperado, resultado);

        verify(sucursalRepository).save(sucursalCaptor.capture());

        Sucursal sucursalEnviada = sucursalCaptor.getValue();

        assertNull(sucursalEnviada.getId());
        verificarCampos(recibido, sucursalEnviada);
    }

    @Test
    void updateSucursal_cuandoSucursalExiste_actualizaSucursalYRetornaDTO() {
        // Arrange
        long id = 1L;

        Sucursal sucursalExistente = crearSucursal(
                1L, "Nombre anterior", "Direccion anterior"
        );
        SucursalDTO recibido = crearDTO(
                null, "Nombre nuevo", "Direccion nueva"
        );
        Sucursal sucursalGuardada = crearSucursal(
                1L, "Nombre nuevo", "Direccion nueva"
        );
        SucursalDTO esperado = crearDTO(
                1L, "Nombre nuevo", "Direccion nueva"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.of(sucursalExistente));
        when(sucursalRepository.save(any(Sucursal.class)))
                .thenReturn(sucursalGuardada);

        // Act
        SucursalDTO resultado = sucursalService.updateSucursal(id, recibido);

        // Assert
        assertEquals(esperado, resultado);

        verify(sucursalRepository).findById(id);
        verify(sucursalRepository).save(sucursalCaptor.capture());

        Sucursal sucursalEnviada = sucursalCaptor.getValue();

        assertEquals(1L, sucursalEnviada.getId());
        verificarCampos(recibido, sucursalEnviada);
    }

    @Test
    void updateSucursal_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long id = 1L;
        SucursalDTO recibido = crearDTO(
                null, "Nombre nuevo", "Direccion nueva"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                SucursalNoEncontradaException.class,
                () -> sucursalService.updateSucursal(id, recibido)
        );

        verify(sucursalRepository).findById(id);
        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void patchSucursal_cuandoSucursalExiste_guardaCambiosYRetornaDTO() {
        // Arrange
        long id = 1L;

        Sucursal sucursalExistente = crearSucursal(
                1L, "Nombre anterior", "Direccion anterior"
        );

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Nombre actualizado");
        body.put("direccion", "Direccion actualizada");

        Sucursal sucursalActualizada = crearSucursal(
                1L, "Nombre actualizado", "Direccion actualizada"
        );
        SucursalDTO esperado = crearDTO(
                1L, "Nombre actualizado", "Direccion actualizada"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.of(sucursalExistente));
        when(jsonMapper.updateValue(sucursalExistente, body))
                .thenReturn(sucursalActualizada);
        when(sucursalRepository.save(sucursalActualizada))
                .thenReturn(sucursalActualizada);

        // Act
        SucursalDTO resultado = sucursalService.patchSucursal(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(sucursalRepository).findById(id);
        verify(jsonMapper).updateValue(sucursalExistente, body);
        verify(sucursalRepository).save(sucursalActualizada);
    }

    @Test
    void patchSucursal_cuandoBodyIncluyeId_ignoraIdGuardaCambiosYRetornaDTO() {
        // Arrange
        long id = 1L;

        Sucursal sucursalExistente = crearSucursal(
                1L, "Nombre anterior", "Direccion anterior"
        );

        Map<String, Object> body = new HashMap<>();
        body.put("id", 999L);
        body.put("nombre", "Nombre protegido");

        Sucursal sucursalActualizada = crearSucursal(
                1L, "Nombre protegido", "Direccion anterior"
        );
        SucursalDTO esperado = crearDTO(
                1L, "Nombre protegido", "Direccion anterior"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.of(sucursalExistente));
        when(jsonMapper.updateValue(eq(sucursalExistente), anyMap()))
                .thenReturn(sucursalActualizada);
        when(sucursalRepository.save(sucursalActualizada))
                .thenReturn(sucursalActualizada);

        // Act
        SucursalDTO resultado = sucursalService.patchSucursal(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(sucursalRepository).findById(id);
        verify(jsonMapper).updateValue(
                eq(sucursalExistente),
                bodyCaptor.capture()
        );
        verify(sucursalRepository).save(sucursalActualizada);

        Map<String, Object> bodyEnviado = bodyCaptor.getValue();

        assertFalse(bodyEnviado.containsKey("id"));
        assertEquals("Nombre protegido", bodyEnviado.get("nombre"));
    }

    @Test
    void patchSucursal_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long id = 1L;

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Nombre actualizado");

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                SucursalNoEncontradaException.class,
                () -> sucursalService.patchSucursal(id, body)
        );

        verify(sucursalRepository).findById(id);
        verifyNoInteractions(jsonMapper);
        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void deleteSucursal_cuandoSucursalExiste_eliminaSucursal() {
        // Arrange
        long id = 1L;
        Sucursal sucursal = crearSucursal(
                1L, "Sucursal Centro", "Avenida Principal 100"
        );

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.of(sucursal));

        // Act
        sucursalService.deleteSucursal(id);

        // Assert
        verify(sucursalRepository).findById(id);
        verify(sucursalRepository).delete(sucursal);
    }

    @Test
    void deleteSucursal_cuandoSucursalNoExiste_lanzaSucursalNoEncontradaException() {
        // Arrange
        long id = 1L;

        when(sucursalRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                SucursalNoEncontradaException.class,
                () -> sucursalService.deleteSucursal(id)
        );

        verify(sucursalRepository).findById(id);
        verify(sucursalRepository, never()).delete(any(Sucursal.class));
    }

    private Sucursal crearSucursal(
            Long id,
            String nombre,
            String direccion
    ) {
        return Sucursal.builder()
                .id(id)
                .nombre(nombre)
                .direccion(direccion)
                .build();
    }

    private SucursalDTO crearDTO(
            Long id,
            String nombre,
            String direccion
    ) {
        return new SucursalDTO(
                id,
                nombre,
                direccion
        );
    }

    private void verificarCampos(
            SucursalDTO esperado,
            Sucursal actual
    ) {
        assertEquals(esperado.nombre(), actual.getNombre());
        assertEquals(esperado.direccion(), actual.getDireccion());
    }
}
