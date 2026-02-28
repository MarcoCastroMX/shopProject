package com.marco.shopProject.user;

import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;
import com.marco.shopProject.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/usuarios")
    public MostrarUserDTO crearUsuario(@RequestBody CrearUserDTO newUser){
        return userService.crearUsuario(newUser);
    }
}
