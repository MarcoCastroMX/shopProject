package com.marco.shopProject.identity.user.repository;

import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import com.marco.shopProject.identity.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    Page<User> findAllByEstadoAndRolesContains(EstadoEnum estado, Rol rol, Pageable pageable);
    Page<User> findAllUserByEstado(EstadoEnum estado, Pageable pageable);
}
