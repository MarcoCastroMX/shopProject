package com.marco.shopProject.user.service;

import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.mapper.Mapper;
import com.marco.shopProject.rol.entity.Rol;
import com.marco.shopProject.rol.repository.RolRepository;
import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;
import com.marco.shopProject.user.entity.User;
import com.marco.shopProject.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
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
    public List<MostrarUserDTO> obtenerUsuarios(String estado) {
        EstadoEnum estadoBuscado = EstadoEnum.valueOf(estado);

        return userRepository.findUserByEstado(estadoBuscado).stream()
                .map(Mapper::userToMostrarUserDTO)
                .toList();
    }

    @Override
    public List<MostrarUserDTO> obtenerUsuariosPorRol(String rolString, String estado) {

        RolesEnum rolBuscado = RolesEnum.valueOf(rolString);
        EstadoEnum estadoBuscado = EstadoEnum.valueOf(estado);

        Rol rol = rolRepository.findRolByRol(rolBuscado);

        return userRepository.findAllByEstadoAndRolesContains(estadoBuscado,rol).stream()
                .map(Mapper::userToMostrarUserDTO)
                .toList();
    }

    @Override
    public Boolean existeEmail(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);

        return user.isPresent();
    }

    @Override
    public MostrarUserDTO obtenerUsuarioPorId(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Id No Encontrado"));
        return Mapper.userToMostrarUserDTO(user);
    }

    @Override
    public MostrarUserDTO crearUsuario(CrearUserDTO user) {
        if(existeEmail(user.email()))
            throw new RuntimeException("Email Ya Existente");

        User newUser = User.builder()
                .nombre(user.nombre())
                .apellido(user.apellido())
                .email(user.email())
                .password(passwordEncoder.encode(user.password()))
                .telefono(user.telefono())
                .estado(EstadoEnum.ACTIVO)
                .build();

        List<Rol> list = new ArrayList<>();
        for(RolesEnum rol : user.roles()){
            Rol search = rolRepository.findRolByRol(rol);
            if(search != null){
                list.add(search);
                newUser.add(search);
            }else{
                throw new RuntimeException("Rol Inexistente");
            }
        }

        User savedUser = userRepository.save(newUser);

        return Mapper.userToMostrarUserDTO(savedUser);
    }

    @Override
    public MostrarUserDTO actualizacionParcialUsuario(Long id, Map<String, Object> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no Encontrado"));

        body.remove("id");
        body.remove("roles");

        User newUser = jsonMapper.updateValue(user,body);

        userRepository.save(user);
        return Mapper.userToMostrarUserDTO(user);
    }

    @Override
    public void eliminarUsuario(Long id) {
        if(id == 1){
            throw new RuntimeException("No se puede eliminar a ese Usuario");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no Encontrado"));

        user.setEstado(EstadoEnum.ELIMINADO);

        userRepository.save(user);
    }
}
