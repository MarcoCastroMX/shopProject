package com.marco.shopProject.identity.rol.repository;

import com.marco.shopProject.core.tools.enums.RolesEnum;
import com.marco.shopProject.identity.rol.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Rol findRolByRol(RolesEnum rol);
}
