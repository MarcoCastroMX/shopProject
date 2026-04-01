package com.marco.shopProject.identity.user.repository;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    List<User> findAllByEstadoAndRolesContains(EstadoEnum estado, Rol rol);
    Optional<User> findUserByEstado(EstadoEnum estado);
}
