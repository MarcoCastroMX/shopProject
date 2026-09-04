package com.marco.shopProject.catalog.producto.service;

import com.marco.shopProject.catalog.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ProductoService {
    Page<ProductoInventarioDTO> getAllProducts(EstadoEnum estado, Pageable pageable);
    ProductoInventarioDTO obtenerProductoPorId(int id);
    ProductoInventarioDTO createProduct(ProductoInventarioDTO newProducto);
    ProductoInventarioDTO updateProduct(int id, ProductoInventarioDTO newProduct);
    ProductoInventarioDTO partialUpdateProduct(int id, Map<String, Object> bodyArray);
    void deleteProduct(int id);
    ProductoInventarioDTO restoreProduct(int id);
}
