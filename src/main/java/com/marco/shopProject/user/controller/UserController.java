package com.marco.shopProject.user.controller;

import com.marco.shopProject.user.dto.CrearUserDTO;
import com.marco.shopProject.user.dto.MostrarUserDTO;
import com.marco.shopProject.user.entity.User;
import com.marco.shopProject.user.service.UserService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<MostrarUserDTO>> obtenerUsuarios(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "ACTIVO") String estado){

        if(role != null){
            return ResponseEntity.ok().body(userService.obtenerUsuariosPorRol(role,estado));
        }
        return ResponseEntity.ok().body(userService.obtenerUsuarios(estado));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<MostrarUserDTO> obtenerUsuarioPorId(@PathVariable Long id){
        return ResponseEntity.ok().body(userService.obtenerUsuarioPorId(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<MostrarUserDTO> crearUsuario(@RequestBody CrearUserDTO newUser){
        MostrarUserDTO user = userService.crearUsuario(newUser);

        URI location = URI.create("/usuarios/"+user.id());

        return ResponseEntity.created(location).body(user);
    }

    @PatchMapping("/usuarios/{id}")
    public ResponseEntity<MostrarUserDTO> actualizacionParcialUsuario(@PathVariable Long id, @RequestBody Map<String,Object> body){
        MostrarUserDTO dto = userService.actualizacionParcialUsuario(id,body);

        return ResponseEntity.accepted().body(dto);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id){
        userService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }
}
