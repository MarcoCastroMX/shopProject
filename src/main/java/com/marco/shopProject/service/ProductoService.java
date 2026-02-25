package com.marco.shopProject.service;

import com.marco.shopProject.dto.ProductoInventarioDTO;

import java.util.List;
import java.util.Map;

public interface ProductoService {
    List<ProductoInventarioDTO> getAllProducts();
    ProductoInventarioDTO obtenerProductoPorId(int id);
    ProductoInventarioDTO createProduct(ProductoInventarioDTO newProducto);
    ProductoInventarioDTO updateProduct(int id, ProductoInventarioDTO newProduct);
    ProductoInventarioDTO partialUpdateProduct(int id, Map<String, Object> bodyArray);
    void deleteProduct(int id);
}
