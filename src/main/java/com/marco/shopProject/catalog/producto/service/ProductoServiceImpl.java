package com.marco.shopProject.catalog.producto.service;

import com.marco.shopProject.catalog.producto.dto.ProductoInventarioDTO;
import com.marco.shopProject.catalog.producto.entity.Producto;
import com.marco.shopProject.catalog.producto.exception.ProductoNoEncontradoException;
import com.marco.shopProject.core.tools.enums.EstadoEnum;
import com.marco.shopProject.core.tools.mapper.Mapper;
import com.marco.shopProject.catalog.producto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.json.JsonMapper;

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
    public Page<ProductoInventarioDTO> getAllProducts(EstadoEnum estado, Pageable pageable) {
        Page<Producto> productos = productoRepository.findAllByEstado(estado, pageable);
        return productos.map(Mapper::toDTO);
    }

    @Override
    public ProductoInventarioDTO obtenerProductoPorId(int id) {
        Producto producto = productoRepository.findByIdAndEstado(Long.valueOf(id), EstadoEnum.ACTIVO)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        return Mapper.toDTO(producto);
    }

    @Override
    public ProductoInventarioDTO createProduct(ProductoInventarioDTO newProducto) {
        Producto producto = Mapper.toDTO(newProducto);
        producto.setEstado(EstadoEnum.ACTIVO);
        return Mapper.toDTO(productoRepository.save(producto));
    }

    @Override
    public ProductoInventarioDTO updateProduct(int id, ProductoInventarioDTO newProduct) {
        Producto old = productoRepository.findByIdAndEstado(Long.valueOf(id), EstadoEnum.ACTIVO)
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
        Producto producto = productoRepository.findByIdAndEstado(Long.valueOf(id), EstadoEnum.ACTIVO)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        bodyArray.remove("id");
        bodyArray.remove("estado");

        Producto nuevoProducto = jsonMapper.updateValue(producto,bodyArray);

        return Mapper.toDTO(productoRepository.save(nuevoProducto));
    }

    @Override
    public void deleteProduct(int id) {
        Producto tempProduct = productoRepository.findById(Long.valueOf(id))
                        .orElseThrow(() -> new ProductoNoEncontradoException(id));

        if(tempProduct.getEstado() == EstadoEnum.ACTIVO){
            tempProduct.setEstado(EstadoEnum.ELIMINADO);
            productoRepository.save(tempProduct);
        }
    }

    @Override
    public ProductoInventarioDTO restoreProduct(int id) {
        Producto producto = productoRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        if(producto.getEstado() == EstadoEnum.ELIMINADO){
            producto.setEstado(EstadoEnum.ACTIVO);
            producto = productoRepository.save(producto);
        }

        return Mapper.toDTO(producto);
    }
}
