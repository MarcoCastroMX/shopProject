package com.marco.shopProject.catalog.producto.repository;

import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Long> {
    Page<Producto> findAll(Pageable pageable);
    Page<Producto> findAllByEstado(EstadoEnum estado, Pageable pageable);
    Optional<Producto> findByIdAndEstado(Long id, EstadoEnum estado);
}
