package com.marco.shopProject.user.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.marco.shopProject.auth.entity.Token;
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

    @Column(unique = true)
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
    @JsonManagedReference
    private List<Rol> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Token> tokens;

    public void addRol(Rol rol){
        if(roles == null){
            roles = new ArrayList<>();
        }
        if(!roles.contains(rol)){
            roles.add(rol);
            rol.add(this);
        }
    }

    public void addToken(Token token){
        if(tokens == null){
            tokens = new ArrayList<>();
        }
        if(!tokens.contains(token)){
            tokens.add(token);
            token.setUser(this);
        }
    }
}
