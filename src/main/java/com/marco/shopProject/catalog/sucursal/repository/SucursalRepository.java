package com.marco.shopProject.catalog.sucursal.repository;

import com.marco.shopProject.catalog.sucursal.entity.Sucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal,Long> {
    Page<Sucursal> findAll(Pageable pageable);
}
