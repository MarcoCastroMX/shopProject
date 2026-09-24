package com.marco.shopProject.security.jwt;

import com.marco.shopProject.security.jwt.dto.AccessTokenValidado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AccessTokenValidator accessTokenValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(request.getServletPath().contains("api/auth")){
            filterChain.doFilter(request,response);
            return;
        }

        Optional<AccessTokenValidado> resultado =
                accessTokenValidator.validateAccessToken(
                        request.getHeader(HttpHeaders.AUTHORIZATION)
                );

        if (resultado.isPresent()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            AccessTokenValidado token = resultado.get();

            List<SimpleGrantedAuthority> authorities = token.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    token.email(),
                    "",
                    true,
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
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }
}
