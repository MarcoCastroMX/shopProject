package com.marco.shopProject.identity.rol.controller;

import com.marco.shopProject.identity.rol.dto.RolDTO;
import com.marco.shopProject.identity.rol.service.RolService;
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

}
