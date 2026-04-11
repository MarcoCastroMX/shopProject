package com.marco.shopProject.catalog.producto.controller;

import com.marco.shopProject.catalog.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.catalog.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService){
        this.productoService = productoService;
    }

    @GetMapping("productos")
    public ResponseEntity<Page<ProductoInventarioDTO>> getAllProducts(@ParameterObject @PageableDefault(size = 20, page = 0) Pageable pageable){
        return ResponseEntity.ok(productoService.getAllProducts(pageable));
    }

    @GetMapping("productos/{id}")
    public ResponseEntity<ProductoInventarioDTO> obtenerProductoPorId(@PathVariable int id){
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    @PostMapping("productos")
    public ResponseEntity<ProductoInventarioDTO> createProduct(@Valid @RequestBody ProductoInventarioDTO nuevoProducto){
        ProductoInventarioDTO producto = productoService.createProduct(nuevoProducto);
        URI location = URI.create("/productos/"+producto.id());
        return ResponseEntity.created(location).body(producto);
    }

    @PutMapping("productos/{id}")
    public ResponseEntity<ProductoInventarioDTO> updateProduct(@PathVariable int id, @Valid @RequestBody ProductoInventarioDTO newProduct){
        return ResponseEntity.ok(productoService.updateProduct(id,newProduct));
    }

    @PatchMapping("productos/{id}")
    public ResponseEntity<ProductoInventarioDTO> partialUpdateProduct(@PathVariable int id, @RequestBody Map<String, Object> bodyArray){
        return ResponseEntity.ok(productoService.partialUpdateProduct(id,bodyArray));
    }

    @DeleteMapping("productos/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id){
        productoService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
