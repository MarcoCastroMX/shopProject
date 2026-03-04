package com.marco.shopProject.security.authentication;

import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.rol.entity.Rol;
import com.marco.shopProject.user.entity.User;
import com.marco.shopProject.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario No Encontrado"));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Rol::getRol)
                .map(Enum::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();

        org.springframework.security.core.userdetails.User newUser = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEstado() == EstadoEnum.ACTIVO,
                true,
                true,
                true,
                authorities);

        return newUser;
    }
}
