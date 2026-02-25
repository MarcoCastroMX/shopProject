package com.marco.shopProject.producto.service;

import com.marco.shopProject.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.producto.entity.Producto;
import com.marco.shopProject.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.mapper.Mapper;
import com.marco.shopProject.producto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Service
public class ProductoServiceImpl implements ProductoService{

    private ProductoRepository productoRepository;
    private JsonMapper jsonMapper;

    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository, JsonMapper jsonMapper){
        this.productoRepository = productoRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public List<ProductoInventarioDTO> getAllProducts() {
        return productoRepository.findAll().stream().map(Mapper::toDTO).toList();
    }

    @Override
    public ProductoInventarioDTO obtenerProductoPorId(int id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        return Mapper.toDTO(producto);
    }

    @Override
    public ProductoInventarioDTO createProduct(ProductoInventarioDTO newProducto) {
        Producto producto = Mapper.toDTO(newProducto);
        return Mapper.toDTO(productoRepository.save(producto));
    }

    @Override
    public ProductoInventarioDTO updateProduct(int id, ProductoInventarioDTO newProduct) {
        Producto old = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        old.setNombre(newProduct.nombre());
        old.setCategoria(newProduct.categoria());
        old.setPrecio(newProduct.precio());
        old.setCantidad(newProduct.cantidad());

        return Mapper.toDTO(productoRepository.save(old));
    }

    @Override
    public ProductoInventarioDTO partialUpdateProduct(int id, @RequestBody Map<String,Object> bodyArray) {
        //Obtener prodcuto de base de datos
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        bodyArray.remove("id");

        Producto nuevoProducto = jsonMapper.updateValue(producto,bodyArray);

        return Mapper.toDTO(nuevoProducto);
    }

    @Override
    public void deleteProduct(int id) {
        Producto tempProduct = productoRepository.findById(id)
                        .orElseThrow(() -> new ProductoNoEncontradoException(id));
        productoRepository.delete(tempProduct);
    }
}
