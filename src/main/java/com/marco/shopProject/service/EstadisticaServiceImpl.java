package com.marco.shopProject.service;

import com.marco.shopProject.dto.ProductoMasVendidoDTO;
import com.marco.shopProject.repository.DetalleVentaRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EstadisticaServiceImpl implements EstadisticaService{

    private DetalleVentaRepository detalleVentaRepository;

    public EstadisticaServiceImpl(DetalleVentaRepository detalleVentaRepository){
        this.detalleVentaRepository = detalleVentaRepository;
    }


    @Override
    public ProductoMasVendidoDTO obtenerProductoMasVendido() {
        List<Object[]> productosPorVenta = detalleVentaRepository.ventasPorProducto();

        return productosPorVenta.stream()
                .map(p -> ProductoMasVendidoDTO.builder()
                        .id((Long) p[0])
                        .nombre((String) p[1])
                        .cantidad(((Number) p[2]).intValue())
                        .build())
                .max(Comparator.comparing(ProductoMasVendidoDTO::cantidad)).orElse(null);
    }
}
