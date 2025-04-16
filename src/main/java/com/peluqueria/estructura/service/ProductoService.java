package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(String id) {
        return productoRepository.findById(id);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public void deleteById(String id) {
        productoRepository.deleteById(id);
    }
    
    /**
     * Actualiza el stock de un producto
     * 
     * @param id ID del producto
     * @param cantidad Cantidad a añadir (positiva) o restar (negativa) del stock
     * @return Producto actualizado o null si no se encuentra
     */
    public Producto actualizarStock(String id, int cantidad) {
        Optional<Producto> optProducto = productoRepository.findById(id);
        if (optProducto.isPresent()) {
            Producto producto = optProducto.get();
            int nuevoStock = producto.getStock() + cantidad;
            if (nuevoStock >= 0) {
                producto.setStock(nuevoStock);
                return productoRepository.save(producto);
            }
        }
        return null;
    }
}
