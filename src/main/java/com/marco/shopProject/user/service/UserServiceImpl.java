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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<MostrarUserDTO> obtenerUsuarios() {
        return List.of();
    }

    @Override
    public List<MostrarUserDTO> obtenerUsuariosPorRol(String RolEnum) {
        return List.of();
    }

    @Override
    public Boolean existeEmail(String email) {
        User user = userRepository.findUserByEmail(email);
        return (user != null) ? true : false;
    }

    @Override
    public MostrarUserDTO obtenerUsuarioPorId(Long id) {
        return null;
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
    public MostrarUserDTO actualizarUsuario(User newUser) {
        return null;
    }

    @Override
    public MostrarUserDTO actualizacionParcialUsuario(Map<String, Object> body) {
        return null;
    }

    @Override
    public void eliminarUsuario(Long id) {

    }
}
