package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public Producto createProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto updateProducto(Long id, Producto producto) {
        Producto existingProducto = getProductoById(id);
        existingProducto.setNombre(producto.getNombre());
        existingProducto.setTipo(producto.getTipo());
        existingProducto.setPrecio(producto.getPrecio());
        existingProducto.setStock(producto.getStock());
        return productoRepository.save(existingProducto);
    }

    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }
}
