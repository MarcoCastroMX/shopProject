package com.marco.shopProject.detalleVenta.repository;

import com.marco.shopProject.detalleVenta.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query(value = """
        SELECT p.id, p.nombre, SUM(d.cantidad)
        FROM detalle_venta AS d
        JOIN Producto AS p
        ON d.producto_id = p.id
        GROUP BY p.id, p.nombre              
        """,nativeQuery = true)
    List<Object[]> ventasPorProducto();
}
