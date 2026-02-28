package com.marco.shopProject.rol.entity;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.user.entity.User;
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
    private List<User> users;

    public void add(User user) {
        if(users == null){
            users = new ArrayList<>();
        }
        if(!users.contains(user)){
            users.add(user);
            user.add(this);
        }
    }
}
