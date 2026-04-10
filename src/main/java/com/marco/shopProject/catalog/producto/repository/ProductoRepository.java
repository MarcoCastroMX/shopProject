package com.marco.shopProject.catalog.producto.repository;

import com.marco.shopProject.catalog.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Integer> {
    Page<Producto> findAll(Pageable pageable);
}
