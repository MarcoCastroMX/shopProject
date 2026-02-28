package com.marco.shopProject.rol.controller;

import com.marco.shopProject.rol.dto.RolDTO;
import com.marco.shopProject.rol.service.RolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping("/roles")
    public List<RolDTO> obtenerRoles(){
        return rolService.obtenerRoles();
    }

    @PostMapping("/roles")
    public RolDTO crearRol(@RequestBody RolDTO rol){
        return rolService.crearRol(rol);
    }
}
