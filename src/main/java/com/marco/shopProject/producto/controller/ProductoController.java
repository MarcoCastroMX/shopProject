package com.marco.shopProject.producto.controller;

import com.marco.shopProject.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
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
    public ResponseEntity<List<ProductoInventarioDTO>> getAllProducts(){
        return ResponseEntity.ok(productoService.getAllProducts());
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
