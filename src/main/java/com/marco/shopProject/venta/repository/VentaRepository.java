package com.marco.shopProject.venta.repository;

import com.marco.shopProject.venta.entity.Venta;
import com.marco.shopProject.enums.EstadoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta,Long> {

    List<Venta> findAllByEstado(EstadoEnum estado);
    List<Venta> findAllBySucursalIdAndFechaBetween(Long sucursalID, LocalDateTime inicio, LocalDateTime fin);
}
