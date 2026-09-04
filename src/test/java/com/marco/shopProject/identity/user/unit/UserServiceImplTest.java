package com.marco.shopProject.identity.user.unit;

import com.marco.shopProject.core.exception.SuperUserException;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.identity.user.dto.CrearUserDTO;
import com.marco.shopProject.identity.user.dto.MostrarUserDTO;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.EmailAlreadyTakenException;
import com.marco.shopProject.identity.user.exception.EstadoInvalidoException;
import com.marco.shopProject.identity.user.exception.RolInvalidoException;
import com.marco.shopProject.identity.user.exception.UserNotFoundException;
import com.marco.shopProject.identity.user.repository.UserRepository;
import com.marco.shopProject.identity.user.service.UserServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Captor
    private ArgumentCaptor<User> usuarioCaptor;

    @Captor
    private ArgumentCaptor<EstadoEnum> estadoCaptor;

    @Captor
    private ArgumentCaptor<RolesEnum> rolCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> bodyCaptor;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void crearUsuario_cuandoEmailNoExiste_guardaUsuarioConRolesYRetornaDTO() {
        // Arrange
        CrearUserDTO recibido = new CrearUserDTO(
                "Marco",
                "Perez",
                "marco@example.com",
                "Clave123!",
                "5512345678",
                List.of(RolesEnum.ROLE_USER, RolesEnum.ROLE_MANAGER)
        );

        String passwordCodificada = "Contraseña codificada";
        Rol rolUser = crearRol(1L, RolesEnum.ROLE_USER);
        Rol rolManager = crearRol(2L, RolesEnum.ROLE_MANAGER);

        User usuarioGuardado = crearUsuario(
                2L, recibido.email(), EstadoEnum.ACTIVO
        );
        MostrarUserDTO esperado = new MostrarUserDTO(
                2L, recibido.email(), "ACTIVO"
        );

        when(userRepository.findUserByEmail(recibido.email()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(recibido.password()))
                .thenReturn(passwordCodificada);
        when(rolRepository.findRolByRol(RolesEnum.ROLE_USER))
                .thenReturn(rolUser);
        when(rolRepository.findRolByRol(RolesEnum.ROLE_MANAGER))
                .thenReturn(rolManager);
        when(userRepository.save(any(User.class)))
                .thenReturn(usuarioGuardado);

        // Act
        MostrarUserDTO resultado = userService.crearUsuario(recibido);

        // Assert
        assertEquals(esperado, resultado);

        verify(userRepository).findUserByEmail(recibido.email());
        verify(passwordEncoder).encode(recibido.password());
        verify(rolRepository).findRolByRol(RolesEnum.ROLE_USER);
        verify(rolRepository).findRolByRol(RolesEnum.ROLE_MANAGER);
        verify(userRepository).save(usuarioCaptor.capture());

        User usuarioEnviado = usuarioCaptor.getValue();

        assertNull(usuarioEnviado.getId());
        assertEquals(recibido.nombre(), usuarioEnviado.getNombre());
        assertEquals(recibido.apellido(), usuarioEnviado.getApellido());
        assertEquals(recibido.email(), usuarioEnviado.getEmail());
        assertEquals(recibido.telefono(), usuarioEnviado.getTelefono());
        assertEquals(passwordCodificada, usuarioEnviado.getPassword());
        assertEquals(EstadoEnum.ACTIVO, usuarioEnviado.getEstado());
        assertEquals(List.of(rolUser, rolManager), usuarioEnviado.getRoles());
    }

    @Test
    void crearUsuario_cuandoEmailExiste_lanzaEmailAlreadyTakenException() {
        // Arrange
        CrearUserDTO recibido = new CrearUserDTO(
                "Marco",
                "Perez",
                "marco@example.com",
                "Clave123!",
                "5512345678",
                List.of(RolesEnum.ROLE_USER)
        );
        User usuarioExistente = crearUsuario(
                2L, recibido.email(), EstadoEnum.ACTIVO
        );

        when(userRepository.findUserByEmail(recibido.email()))
                .thenReturn(Optional.of(usuarioExistente));

        // Act y Assert
        assertThrows(
                EmailAlreadyTakenException.class,
                () -> userService.crearUsuario(recibido)
        );

        verify(userRepository).findUserByEmail(recibido.email());
        verifyNoInteractions(passwordEncoder, rolRepository);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void obtenerUsuarios_cuandoRecibeActivoYExistenUsuarios_retornaPaginaDeDTO() {
        // Arrange
        String estado = "ACTIVO";
        Pageable pageable = PageRequest.of(1, 2);

        User usuario1 = crearUsuario(
                2L, "marco@example.com", EstadoEnum.ACTIVO
        );
        User usuario2 = crearUsuario(
                3L, "ana@example.com", EstadoEnum.ACTIVO
        );

        Page<User> paginaRepositorio = new PageImpl<>(
                List.of(usuario1, usuario2), pageable, 6
        );
        List<MostrarUserDTO> esperado = List.of(
                new MostrarUserDTO(2L, "marco@example.com", "ACTIVO"),
                new MostrarUserDTO(3L, "ana@example.com", "ACTIVO")
        );

        when(userRepository.findAllUserByEstado(
                EstadoEnum.ACTIVO, pageable
        )).thenReturn(paginaRepositorio);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuarios(estado, pageable);

        // Assert
        verificarPagina(resultado, esperado, pageable, 6, 3);
        assertTrue(resultado.hasPrevious());
        assertTrue(resultado.hasNext());

        verify(userRepository).findAllUserByEstado(
                estadoCaptor.capture(), eq(pageable)
        );
        assertEquals(EstadoEnum.ACTIVO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuarios_cuandoRecibeEliminadoConEspacios_normalizaEstadoYRetornaDTO() {
        // Arrange
        String estado = " eliminado ";
        Pageable pageable = PageRequest.of(0, 20);

        User usuario = crearUsuario(
                2L, "marco@example.com", EstadoEnum.ELIMINADO
        );
        Page<User> paginaRepositorio = new PageImpl<>(
                List.of(usuario), pageable, 1
        );
        List<MostrarUserDTO> esperado = List.of(
                new MostrarUserDTO(2L, "marco@example.com", "ELIMINADO")
        );

        when(userRepository.findAllUserByEstado(
                EstadoEnum.ELIMINADO, pageable
        )).thenReturn(paginaRepositorio);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuarios(estado, pageable);

        // Assert
        verificarPagina(resultado, esperado, pageable, 1, 1);
        assertFalse(resultado.hasPrevious());
        assertFalse(resultado.hasNext());

        verify(userRepository).findAllUserByEstado(
                estadoCaptor.capture(), eq(pageable)
        );
        assertEquals(EstadoEnum.ELIMINADO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuarios_cuandoNoExistenUsuarios_retornaPaginaVacia() {
        // Arrange
        String estado = "ACTIVO";
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> paginaVacia = Page.empty(pageable);

        when(userRepository.findAllUserByEstado(
                EstadoEnum.ACTIVO, pageable
        )).thenReturn(paginaVacia);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuarios(estado, pageable);

        // Assert
        assertTrue(resultado.isEmpty());
        verificarPagina(resultado, List.of(), pageable, 0, 0);
        assertFalse(resultado.hasPrevious());
        assertFalse(resultado.hasNext());

        verify(userRepository).findAllUserByEstado(
                estadoCaptor.capture(), eq(pageable)
        );
        assertEquals(EstadoEnum.ACTIVO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuarios_cuandoRecibeEstadoInvalido_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "EN ESPERA";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuarios(estado, pageable)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void obtenerUsuarios_cuandoRecibeEstadoNull_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = null;
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuarios(estado, pageable)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void obtenerUsuarios_cuandoRecibeEstadoVacio_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuarios(estado, pageable)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void obtenerUsuarios_cuandoRecibeEstadoConEspaciosEnBlanco_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "  ";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuarios(estado, pageable)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeActivoYRoleUser_retornaPaginaDeDTO() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = "ROLE_USER";
        Pageable pageable = PageRequest.of(1, 2);
        Rol rol = crearRol(1L, RolesEnum.ROLE_USER);

        User usuario1 = crearUsuario(
                2L, "marco@example.com", EstadoEnum.ACTIVO
        );
        User usuario2 = crearUsuario(
                3L, "ana@example.com", EstadoEnum.ACTIVO
        );
        usuario1.addRol(rol);
        usuario2.addRol(rol);

        Page<User> paginaRepositorio = new PageImpl<>(
                List.of(usuario1, usuario2), pageable, 6
        );
        List<MostrarUserDTO> esperado = List.of(
                new MostrarUserDTO(2L, "marco@example.com", "ACTIVO"),
                new MostrarUserDTO(3L, "ana@example.com", "ACTIVO")
        );

        when(rolRepository.findRolByRol(RolesEnum.ROLE_USER))
                .thenReturn(rol);
        when(userRepository.findAllByEstadoAndRolesContains(
                EstadoEnum.ACTIVO, rol, pageable
        )).thenReturn(paginaRepositorio);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                );

        // Assert
        verificarPagina(resultado, esperado, pageable, 6, 3);
        assertTrue(resultado.hasPrevious());
        assertTrue(resultado.hasNext());

        verify(rolRepository).findRolByRol(rolCaptor.capture());
        verify(userRepository).findAllByEstadoAndRolesContains(
                estadoCaptor.capture(), same(rol), eq(pageable)
        );

        assertEquals(RolesEnum.ROLE_USER, rolCaptor.getValue());
        assertEquals(EstadoEnum.ACTIVO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuariosPorRol_cuandoNoExistenManagersActivos_retornaPaginaVacia() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = "ROLE_MANAGER";
        Pageable pageable = PageRequest.of(0, 20);
        Rol rol = crearRol(2L, RolesEnum.ROLE_MANAGER);
        Page<User> paginaVacia = Page.empty(pageable);

        when(rolRepository.findRolByRol(RolesEnum.ROLE_MANAGER))
                .thenReturn(rol);
        when(userRepository.findAllByEstadoAndRolesContains(
                EstadoEnum.ACTIVO, rol, pageable
        )).thenReturn(paginaVacia);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                );

        // Assert
        assertTrue(resultado.isEmpty());
        verificarPagina(resultado, List.of(), pageable, 0, 0);
        assertFalse(resultado.hasPrevious());
        assertFalse(resultado.hasNext());

        verify(rolRepository).findRolByRol(rolCaptor.capture());
        verify(userRepository).findAllByEstadoAndRolesContains(
                estadoCaptor.capture(), same(rol), eq(pageable)
        );

        assertEquals(RolesEnum.ROLE_MANAGER, rolCaptor.getValue());
        assertEquals(EstadoEnum.ACTIVO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeRoleAdminConEspacios_normalizaRolYRetornaDTO() {
        // Arrange
        String estado = "ELIMINADO";
        String rolString = " role_admin ";
        Pageable pageable = PageRequest.of(0, 20);
        Rol rol = crearRol(3L, RolesEnum.ROLE_ADMIN);

        User usuario = crearUsuario(
                2L, "admin@example.com", EstadoEnum.ELIMINADO
        );
        usuario.addRol(rol);

        Page<User> paginaRepositorio = new PageImpl<>(
                List.of(usuario), pageable, 1
        );
        List<MostrarUserDTO> esperado = List.of(
                new MostrarUserDTO(2L, "admin@example.com", "ELIMINADO")
        );

        when(rolRepository.findRolByRol(RolesEnum.ROLE_ADMIN))
                .thenReturn(rol);
        when(userRepository.findAllByEstadoAndRolesContains(
                EstadoEnum.ELIMINADO, rol, pageable
        )).thenReturn(paginaRepositorio);

        // Act
        Page<MostrarUserDTO> resultado =
                userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                );

        // Assert
        verificarPagina(resultado, esperado, pageable, 1, 1);
        assertFalse(resultado.hasPrevious());
        assertFalse(resultado.hasNext());

        verify(rolRepository).findRolByRol(rolCaptor.capture());
        verify(userRepository).findAllByEstadoAndRolesContains(
                estadoCaptor.capture(), same(rol), eq(pageable)
        );

        assertEquals(RolesEnum.ROLE_ADMIN, rolCaptor.getValue());
        assertEquals(EstadoEnum.ELIMINADO, estadoCaptor.getValue());
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeRolInvalido_lanzaRolInvalidoException() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = "ROLE_SELLER";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                RolInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeRolNull_lanzaRolInvalidoException() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = null;
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                RolInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeRolVacio_lanzaRolInvalidoException() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = "";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                RolInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeRolConEspaciosEnBlanco_lanzaRolInvalidoException() {
        // Arrange
        String estado = "ACTIVO";
        String rolString = "  ";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                RolInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeEstadoNull_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = null;
        String rolString = "ROLE_USER";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeEstadoVacio_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "";
        String rolString = "ROLE_MANAGER";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeEstadoConEspaciosEnBlanco_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "  ";
        String rolString = "ROLE_ADMIN";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void obtenerUsuariosPorRol_cuandoRecibeEstadoInvalido_lanzaEstadoInvalidoException() {
        // Arrange
        String estado = "STAND BY";
        String rolString = "ROLE_USER";
        Pageable pageable = PageRequest.of(0, 20);

        // Act y Assert
        assertThrows(
                EstadoInvalidoException.class,
                () -> userService.obtenerUsuariosPorRol(
                        rolString, estado, pageable
                )
        );

        verifyNoInteractions(rolRepository, userRepository);
    }

    @Test
    void existeEmail_cuandoUsuarioExiste_retornaVerdadero() {
        // Arrange
        String email = "marco@example.com";
        User usuario = crearUsuario(2L, email, EstadoEnum.ACTIVO);

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(usuario));

        // Act
        Boolean resultado = userService.existeEmail(email);

        // Assert
        assertTrue(resultado);
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void existeEmail_cuandoUsuarioNoExiste_retornaFalso() {
        // Arrange
        String email = "marco@example.com";

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        Boolean resultado = userService.existeEmail(email);

        // Assert
        assertFalse(resultado);
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void obtenerUsuarioPorId_cuandoUsuarioExiste_retornaMostrarUserDTO() {
        // Arrange
        Long id = 2L;
        User usuario = crearUsuario(
                id, "marco@example.com", EstadoEnum.ACTIVO
        );
        MostrarUserDTO esperado = new MostrarUserDTO(
                id, "marco@example.com", "ACTIVO"
        );

        when(userRepository.findById(id))
                .thenReturn(Optional.of(usuario));

        // Act
        MostrarUserDTO resultado = userService.obtenerUsuarioPorId(id);

        // Assert
        assertEquals(esperado, resultado);
        verify(userRepository).findById(id);
    }

    @Test
    void obtenerUsuarioPorId_cuandoUsuarioNoExiste_lanzaUserNotFoundException() {
        // Arrange
        Long id = 2L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userService.obtenerUsuarioPorId(id)
        );

        verify(userRepository).findById(id);
    }

    @Test
    void actualizacionParcialUsuario_cuandoBodyNoTieneIdNiRoles_guardaCambiosYRetornaDTO() {
        // Arrange
        Long id = 2L;
        User usuarioExistente = crearUsuario(
                id, "anterior@example.com", EstadoEnum.ACTIVO
        );
        usuarioExistente.setNombre("Nombre anterior");

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Nombre actualizado");
        body.put("email", "actualizado@example.com");

        User usuarioActualizado = crearUsuario(
                id, "actualizado@example.com", EstadoEnum.ACTIVO
        );
        usuarioActualizado.setNombre("Nombre actualizado");

        MostrarUserDTO esperado = new MostrarUserDTO(
                id, "actualizado@example.com", "ACTIVO"
        );

        when(userRepository.findById(id))
                .thenReturn(Optional.of(usuarioExistente));
        when(jsonMapper.updateValue(usuarioExistente, body))
                .thenReturn(usuarioActualizado);
        when(userRepository.save(usuarioActualizado))
                .thenReturn(usuarioActualizado);

        // Act
        MostrarUserDTO resultado =
                userService.actualizacionParcialUsuario(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(userRepository).findById(id);
        verify(jsonMapper).updateValue(usuarioExistente, body);
        verify(userRepository).save(same(usuarioActualizado));
    }

    @Test
    void actualizacionParcialUsuario_cuandoBodyIncluyeIdYRoles_ignoraCamposProtegidos() {
        // Arrange
        Long id = 2L;
        User usuarioExistente = crearUsuario(
                id, "anterior@example.com", EstadoEnum.ACTIVO
        );
        Rol rolUser = crearRol(1L, RolesEnum.ROLE_USER);
        usuarioExistente.addRol(rolUser);

        Map<String, Object> body = new HashMap<>();
        body.put("id", 999L);
        body.put("roles", List.of(RolesEnum.ROLE_ADMIN));
        body.put("email", "actualizado@example.com");

        User usuarioActualizado = crearUsuario(
                id, "actualizado@example.com", EstadoEnum.ACTIVO
        );
        usuarioActualizado.addRol(rolUser);

        MostrarUserDTO esperado = new MostrarUserDTO(
                id, "actualizado@example.com", "ACTIVO"
        );

        when(userRepository.findById(id))
                .thenReturn(Optional.of(usuarioExistente));
        when(jsonMapper.updateValue(
                same(usuarioExistente), anyMap()
        )).thenReturn(usuarioActualizado);
        when(userRepository.save(usuarioActualizado))
                .thenReturn(usuarioActualizado);

        // Act
        MostrarUserDTO resultado =
                userService.actualizacionParcialUsuario(id, body);

        // Assert
        assertEquals(esperado, resultado);

        verify(userRepository).findById(id);
        verify(jsonMapper).updateValue(
                same(usuarioExistente), bodyCaptor.capture()
        );
        verify(userRepository).save(same(usuarioActualizado));

        Map<String, Object> bodyEnviado = bodyCaptor.getValue();

        assertFalse(bodyEnviado.containsKey("id"));
        assertFalse(bodyEnviado.containsKey("roles"));
        assertEquals(
                "actualizado@example.com",
                bodyEnviado.get("email")
        );
    }

    @Test
    void actualizacionParcialUsuario_cuandoUsuarioNoExiste_lanzaUserNotFoundException() {
        // Arrange
        Long id = 2L;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "actualizado@example.com");

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userService.actualizacionParcialUsuario(id, body)
        );

        verify(userRepository).findById(id);
        verifyNoInteractions(jsonMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void eliminarUsuario_cuandoUsuarioExiste_cambiaEstadoAEliminado() {
        // Arrange
        Long id = 2L;
        User usuario = crearUsuario(
                id, "marco@example.com", EstadoEnum.ACTIVO
        );

        when(userRepository.findById(id))
                .thenReturn(Optional.of(usuario));

        // Act
        userService.eliminarUsuario(id);

        // Assert
        verify(userRepository).findById(id);
        verify(userRepository).save(usuarioCaptor.capture());

        User usuarioEnviado = usuarioCaptor.getValue();

        assertSame(usuario, usuarioEnviado);
        assertEquals(id, usuarioEnviado.getId());
        assertEquals(EstadoEnum.ELIMINADO, usuarioEnviado.getEstado());

        verify(userRepository, never()).delete(any(User.class));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarUsuario_cuandoIntentaEliminarSuperUsuario_lanzaSuperUserException() {
        // Arrange
        Long id = 1L;

        // Act y Assert
        assertThrows(
                SuperUserException.class,
                () -> userService.eliminarUsuario(id)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void eliminarUsuario_cuandoUsuarioNoExiste_lanzaUserNotFoundException() {
        // Arrange
        Long id = 2L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act y Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userService.eliminarUsuario(id)
        );

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any(User.class));
    }

    private User crearUsuario(
            Long id,
            String email,
            EstadoEnum estado
    ) {
        return User.builder()
                .id(id)
                .email(email)
                .estado(estado)
                .build();
    }

    private Rol crearRol(Long id, RolesEnum rol) {
        return Rol.builder()
                .id(id)
                .rol(rol)
                .build();
    }

    private void verificarPagina(
            Page<MostrarUserDTO> resultado,
            List<MostrarUserDTO> contenidoEsperado,
            Pageable pageable,
            long totalElementos,
            int totalPaginas
    ) {
        assertEquals(contenidoEsperado, resultado.getContent());
        assertEquals(
                contenidoEsperado.size(),
                resultado.getNumberOfElements()
        );
        assertEquals(totalElementos, resultado.getTotalElements());
        assertEquals(totalPaginas, resultado.getTotalPages());
        assertEquals(pageable.getPageNumber(), resultado.getNumber());
        assertEquals(pageable.getPageSize(), resultado.getSize());
        assertEquals(pageable, resultado.getPageable());
    }
}
