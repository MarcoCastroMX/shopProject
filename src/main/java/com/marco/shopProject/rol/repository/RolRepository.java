package com.marco.shopProject.rol.repository;

import com.marco.shopProject.enums.RolesEnum;
import com.marco.shopProject.rol.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Rol findRolByRol(RolesEnum rol);
}
