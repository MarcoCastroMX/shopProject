package com.marco.shopProject.catalog.sucursal.repository;

import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal,Long> {
}
