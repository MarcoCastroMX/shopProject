package com.marco.shopProject.core.init;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.rol.repository.RolRepository;
import com.marco.shopProject.identity.user.entity.User;
import com.marco.shopProject.identity.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

@Component
public class SuperUsuario implements CommandLineRunner {

    @Value("${admin.password}")
    String password;

    @Value("${admin.username}")
    String email;

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperUsuario(UserRepository userRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Map<RolesEnum, Rol> rolesPorTipo = new EnumMap<>(RolesEnum.class);

        for(RolesEnum tipoRol : RolesEnum.values()){
            Rol rol = rolRepository.findRolByRol(tipoRol);

            if(rol == null){
                rol = Rol.builder()
                        .rol(tipoRol)
                        .users(new ArrayList<>())
                        .build();

                rol = rolRepository.save(rol);
            }

            rolesPorTipo.put(tipoRol, rol);
        }

        User user = userRepository.findUserByEmail(email)
                .orElse(new User());

        if(user.getEmail() == null){
            user =  User.builder()
                    .nombre("ADMIN")
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .estado(EstadoEnum.ACTIVO)
                    .build();

            user.addRol(rolesPorTipo.get(RolesEnum.ROLE_ADMIN));
            userRepository.save(user);
        }
    }
}
