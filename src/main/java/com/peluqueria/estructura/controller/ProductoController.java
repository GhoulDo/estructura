package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable String id) {
        return productoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> createProducto(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.save(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable String id, @RequestBody Producto producto) {
        return productoService.findById(id)
                .map(existingProducto -> {
                    producto.setId(id);
                    return ResponseEntity.ok(productoService.save(producto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Producto> patchProducto(@PathVariable String id, @RequestBody Producto productoActualizado) {
        return productoService.findById(id)
                .map(existingProducto -> {
                    // Actualiza solo los campos no nulos
                    if (productoActualizado.getNombre() != null) {
                        existingProducto.setNombre(productoActualizado.getNombre());
                    }
                    if (productoActualizado.getTipo() != null) {
                        existingProducto.setTipo(productoActualizado.getTipo());
                    }
                    if (productoActualizado.getPrecio() != null) {
                        existingProducto.setPrecio(productoActualizado.getPrecio());
                    }
                    if (productoActualizado.getStock() > 0) {
                        existingProducto.setStock(productoActualizado.getStock());
                    }
                    if (productoActualizado.getDescripcion() != null) {
                        existingProducto.setDescripcion(productoActualizado.getDescripcion());
                    }
                    if (productoActualizado.getEstado() != null) {
                        existingProducto.setEstado(productoActualizado.getEstado());
                    }

                    return ResponseEntity.ok(productoService.save(existingProducto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
