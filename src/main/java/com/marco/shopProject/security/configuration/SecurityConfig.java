package com.marco.shopProject.security.configuration;

import com.marco.shopProject.auth.entity.Token;
import com.marco.shopProject.auth.repository.TokenRepository;
import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.entity.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final TokenRepository tokenRepository;

    public SecurityConfig(UserDetailsService userDetailsService, AuthenticationProvider authenticationProvider, JwtAuthFilter jwtAuthFilter, TokenRepository tokenRepository) {
        this.userDetailsService = userDetailsService;
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
        this.tokenRepository = tokenRepository;
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
                        .requestMatchers("/api/auth/**").permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/api/auth/logout")
                                .addLogoutHandler((request, response, authentication) -> {
                                    final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                                    logout(authHeader);
                                }).logoutSuccessHandler((request, response, authentication) ->
                                        SecurityContextHolder.clearContext()));


        return http.build();
    }
    private void logout(final String token){
        if(token == null || !token.startsWith("Bearer ")){
            throw new IllegalArgumentException("Invalid Token");
        }

        final String jwtToken = token.substring(7);
        final Token foundToken = tokenRepository.findByToken(jwtToken);
        foundToken.setExpired(true);
        foundToken.setRevoked(true);
        tokenRepository.save(foundToken);
    }

    @Bean
    public AuthenticationProvider createProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withRolePrefix("")
                .role(RolesEnum.ROLE_ADMIN.name()).implies(RolesEnum.ROLE_MANAGER.name())
                .role(RolesEnum.ROLE_MANAGER.name()).implies(RolesEnum.ROLE_USER.name())
                .build();
    }
}
