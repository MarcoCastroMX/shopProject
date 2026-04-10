package com.marco.shopProject.sales.venta.repository;

import com.marco.shopProject.sales.venta.entity.Venta;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta,Long> {
    Page<Venta> findAll(Pageable pageable);
    Page<Venta> findAllByEstado(EstadoEnum estado, Pageable pageable);
    Page<Venta> findAllBySucursalIdAndFechaBetween(Long sucursalID, LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
