package com.marco.shopProject.identity.user.service;

import com.marco.shopProject.identity.user.exception.EstadoInvalidoException;
import com.marco.shopProject.identity.user.exception.RolInvalidoException;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.core.tools.mapper.Mapper;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.identity.user.dto.CrearUserDTO;
import com.marco.shopProject.identity.user.dto.MostrarUserDTO;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.exception.EmailAlreadyTakenException;
import com.marco.shopProject.core.exception.SuperUserException;
import com.marco.shopProject.identity.user.exception.UserNotFoundException;
import com.marco.shopProject.identity.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    private final JsonMapper jsonMapper;

    public UserServiceImpl(UserRepository userRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, JsonMapper jsonMapper){
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Page<MostrarUserDTO> obtenerUsuarios(String estado, Pageable pageable) {
        EstadoEnum estadoBuscado = convertirEstado(estado);

        return userRepository.findAllUserByEstado(estadoBuscado, pageable)
                .map(Mapper::userToMostrarUserDTO);
    }

    @Override
    public Page<MostrarUserDTO> obtenerUsuariosPorRol(String rolString, String estado, Pageable pageable) {

        RolesEnum rolBuscado = convertirRol(rolString);
        EstadoEnum estadoBuscado = convertirEstado(estado);

        Rol rol = rolRepository.findRolByRol(rolBuscado);

        return userRepository.findAllByEstadoAndRolesContains(estadoBuscado,rol, pageable)
                .map(Mapper::userToMostrarUserDTO);
    }

    @Override
    public Boolean existeEmail(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);

        return user.isPresent();
    }

    @Override
    public MostrarUserDTO obtenerUsuarioPorId(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new UserNotFoundException("User No Encontrado"));
        return Mapper.userToMostrarUserDTO(user);
    }

    @Override
    public MostrarUserDTO crearUsuario(CrearUserDTO user) {
        if(existeEmail(user.email()))
            throw new EmailAlreadyTakenException("Email Ya Existente");

        User newUser = User.builder()
                .nombre(user.nombre())
                .apellido(user.apellido())
                .email(user.email())
                .password(passwordEncoder.encode(user.password()))
                .telefono(user.telefono())
                .estado(EstadoEnum.ACTIVO)
                .build();

        for(RolesEnum rol : user.roles()){
            Rol search = rolRepository.findRolByRol(rol);
            newUser.addRol(search);
        }

        return Mapper.userToMostrarUserDTO(userRepository.save(newUser));
    }

    @Override
    public MostrarUserDTO actualizacionParcialUsuario(Long id, Map<String, Object> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no Encontrado"));

        body.remove("id");
        body.remove("roles");

        User newUser = jsonMapper.updateValue(user,body);

        return Mapper.userToMostrarUserDTO(userRepository.save(newUser));
    }

    @Override
    public void eliminarUsuario(Long id) {
        if(id == 1){
            throw new SuperUserException("No se puede eliminar a ese Usuario");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no Encontrado"));

        user.setEstado(EstadoEnum.ELIMINADO);

        userRepository.save(user);
    }

    private EstadoEnum convertirEstado(String estado) {
        if(estado == null || estado.isBlank()){
            throw new EstadoInvalidoException(estado);
        }

        try{
            return EstadoEnum.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        }catch(IllegalArgumentException exception){
            throw new EstadoInvalidoException(estado);
        }
    }

    private RolesEnum convertirRol(String rol) {
        if(rol == null || rol.isBlank()){
            throw new RolInvalidoException(rol);
        }

        try{
            return RolesEnum.valueOf(rol.trim().toUpperCase(Locale.ROOT));
        }catch(IllegalArgumentException exception){
            throw new RolInvalidoException(rol);
        }
    }
}
