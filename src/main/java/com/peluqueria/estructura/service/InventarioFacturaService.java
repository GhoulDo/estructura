package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioFacturaService {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    @Autowired
    public InventarioFacturaService(ProductoRepository productoRepository, ProductoService productoService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }
    
    /**
     * Verifica si hay suficiente stock para todos los productos de una lista de detalles
     * @throws RuntimeException si no hay suficiente stock
     */
    public void verificarStockSuficiente(List<DetalleFactura> detalles) {
        if (detalles == null) return;
        
        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                Producto producto = productoRepository.findById(detalle.getProductoId())
                        .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado con ID: " + detalle.getProductoId()));
                
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new RuntimeException(
                        "Stock insuficiente para el producto: " + producto.getNombre());
                }
            }
        }
    }
    
    /**
     * Actualiza el inventario después de crear o modificar una factura
     */
    @Transactional
    public void actualizarInventario(List<DetalleFactura> detalles) {
        if (detalles == null) return;
        
        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                productoService.actualizarStock(detalle.getProductoId(), -detalle.getCantidad());
            }
        }
    }
    
    /**
     * Restaura el inventario cuando se anula una factura
     */
    @Transactional
    public void restaurarInventario(List<DetalleFactura> detalles) {
        if (detalles == null) return;
        
        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                productoService.actualizarStock(detalle.getProductoId(), detalle.getCantidad());
            }
        }
    }
}
