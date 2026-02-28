package com.marco.shopProject.user.entity;

import com.marco.shopProject.enums.EstadoEnum;
import com.marco.shopProject.rol.entity.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String apellido;

    private String email;

    private String password;

    private String telefono;

    @Enumerated(EnumType.STRING)
    private EstadoEnum estado;

    @ManyToMany(cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH},
                fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private List<Rol> roles;

    public void add(Rol rol){
        if(roles == null){
            roles = new ArrayList<>();
        }
        if(!roles.contains(rol)){
            roles.add(rol);
            rol.add(this);
        }
    }
}
