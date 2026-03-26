package com.marco.shopProject.auth.controller;

import com.marco.shopProject.auth.dto.LoginRequestDTO;
import com.marco.shopProject.auth.dto.RegisterRequestDTO;
import com.marco.shopProject.auth.dto.TokenResponseDTO;
import com.marco.shopProject.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody final RegisterRequestDTO request){
        final TokenResponseDTO token = service.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> authenticate(@RequestBody final LoginRequestDTO request){
        final TokenResponseDTO token = service.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public TokenResponseDTO refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader){
        return service.refreshToken(authHeader);
    }
}
