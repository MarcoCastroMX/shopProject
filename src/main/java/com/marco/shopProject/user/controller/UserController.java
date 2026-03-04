package com.marco.shopProject.user.controller;

import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;
import com.marco.shopProject.user.service.UserService;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/usuarios")
    public List<MostrarUserDTO> obtenerUsuarios(
            @RequestParam(required = false) String role){
        if(role != null){
            return userService.obtenerUsuariosPorRol(role);
        }
        return userService.obtenerUsuarios();
    }

    @GetMapping("/usuarios/{id}")
    public MostrarUserDTO obtenerUsuarioPorId(@PathVariable Long id){
        return userService.obtenerUsuarioPorId(id);
    }

    @PostMapping("/usuarios")
    public MostrarUserDTO crearUsuario(@RequestBody CrearUserDTO newUser){
        return userService.crearUsuario(newUser);
    }
}
