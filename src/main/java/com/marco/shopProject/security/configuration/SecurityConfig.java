package com.marco.shopProject.security.configuration;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.entity.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http    .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE,"/api/ventas/**").hasAuthority(RolesEnum.ROLE_MANAGER.name())
                        .requestMatchers("/api/ventas/**").hasAuthority(RolesEnum.ROLE_USER.name())
                        .requestMatchers(HttpMethod.DELETE,"/api/sucursales/**").hasAuthority(RolesEnum.ROLE_MANAGER.name())
                        .requestMatchers("/api/sucursales/**").hasAuthority(RolesEnum.ROLE_USER.name())
                        .requestMatchers(HttpMethod.DELETE,"/api/productos/**").hasAuthority(RolesEnum.ROLE_MANAGER.name())
                        .requestMatchers("/api/productos/**").hasAuthority(RolesEnum.ROLE_USER.name())
                        .requestMatchers("/api/estadistica/**").hasAuthority(RolesEnum.ROLE_MANAGER.name())
                        .requestMatchers("/api/usuarios/**").hasAuthority(RolesEnum.ROLE_ADMIN.name())
                        .requestMatchers("/api/roles/**").hasAuthority(RolesEnum.ROLE_ADMIN.name())
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider createProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withRolePrefix("")
                .role(RolesEnum.ROLE_ADMIN.name()).implies(RolesEnum.ROLE_MANAGER.name())
                .role(RolesEnum.ROLE_MANAGER.name()).implies(RolesEnum.ROLE_USER.name())
                .build();
    }
}
