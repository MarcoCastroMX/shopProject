package com.marco.shopProject.security.configuration;

import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.entity.Rol;
import com.marco.shopProject.rol.repository.RolRepository;
import com.marco.shopProject.user.entity.User;
import com.marco.shopProject.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        List<Rol> rolList = rolRepository.findAll();
        if(rolList.isEmpty()){

            for(RolesEnum roles : RolesEnum.values()){
                Rol rol = Rol.builder()
                        .rol(roles)
                        .users(new ArrayList<>())
                        .build();

                rolRepository.save(rol);
                rolList.add(rol);
            }
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

            user.addRol(rolList.getFirst());
            userRepository.save(user);
        }
    }
}
