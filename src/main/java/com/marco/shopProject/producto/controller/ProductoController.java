package com.marco.shopProject.producto.controller;

import com.marco.shopProject.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.producto.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService){
        this.productoService = productoService;
    }

    @GetMapping("productos")
    public List<ProductoInventarioDTO> getAllProducts(){
        return productoService.getAllProducts();
    }

    @GetMapping("productos/{id}")
    public ProductoInventarioDTO obtenerProductoPorId(@PathVariable int id){
        return productoService.obtenerProductoPorId(id);
    }

    @PostMapping("productos")
    public ProductoInventarioDTO createProduct(@RequestBody ProductoInventarioDTO nuevoProducto){
        return productoService.createProduct(nuevoProducto);
    }

    @PutMapping("productos/{id}")
    public ProductoInventarioDTO updateProduct(@PathVariable int id, @RequestBody ProductoInventarioDTO newProduct){
        return productoService.updateProduct(id,newProduct);
    }

    @PatchMapping("productos/{id}")
    public ProductoInventarioDTO partialUpdateProduct(@PathVariable int id, @RequestBody Map<String, Object> bodyArray){
        return productoService.partialUpdateProduct(id,bodyArray);
    }

    @DeleteMapping("productos/{id}")
    public String deleteProduct(@PathVariable int id){
        productoService.deleteProduct(id);
        return "Producto Eliminado";
    }

}
