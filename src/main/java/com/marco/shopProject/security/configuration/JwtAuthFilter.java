package com.marco.shopProject.security.configuration;

import com.marco.shopProject.auth.service.JwtService;
import com.marco.shopProject.enums.EstadoEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(request.getServletPath().contains("api/auth")){
            filterChain.doFilter(request,response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        final String jwtToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwtToken);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if(jwtToken == null || jwtService.isTokenExpired(jwtToken) ){
                filterChain.doFilter(request,response);
                return;
            }

            final List<String> rolList = jwtService.extractRoles(jwtToken);
            final String userEstado = jwtService.extractEstado(jwtToken);
            final boolean isActive = EstadoEnum.ACTIVO.toString().equals(userEstado);

            if(rolList.isEmpty() || !isActive){
                filterChain.doFilter(request,response);
                return;
            }


            List<SimpleGrantedAuthority> authorities = rolList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                    userEmail,
                    "",
                    isActive,
                    true,
                    true,
                    true,
                    authorities);

            final var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request,response);
    }
}
