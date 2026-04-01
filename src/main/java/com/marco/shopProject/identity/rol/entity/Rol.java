package com.marco.shopProject.identity.rol.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rol")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RolesEnum rol;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    private List<User> users;


    public void add(User user) {
        if(users == null){
            users = new ArrayList<>();
        }
        if(!users.contains(user)){
            users.add(user);
            user.addRol(this);
        }
    }
}
