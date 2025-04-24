package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
import com.peluqueria.estructura.exception.StockInsuficienteException;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioFacturaService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioFacturaService.class);
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public InventarioFacturaService(ProductoRepository productoRepository, ProductoService productoService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }

    /**
     * Verifica si hay suficiente stock para todos los productos de una lista de
     * detalles
     * 
     * @throws StockInsuficienteException si no hay suficiente stock
     * @throws ResourceNotFoundException  si no se encuentra algún producto
     */
    public void verificarStockSuficiente(List<DetalleFactura> detalles) {
        logger.debug("Verificando stock para {} detalles", detalles != null ? detalles.size() : 0);
        if (detalles == null)
            return;

        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                Producto producto = productoRepository.findById(detalle.getProductoId())
                        .orElseThrow(() -> {
                            logger.error("Producto no encontrado con ID: {}", detalle.getProductoId());
                            return new ResourceNotFoundException("Producto", "id", detalle.getProductoId());
                        });

                logger.debug("Verificando stock para producto: {}, disponible: {}, solicitado: {}",
                        producto.getNombre(), producto.getStock(), detalle.getCantidad());

                if (producto.getStock() < detalle.getCantidad()) {
                    logger.warn("Stock insuficiente para producto: {}, disponible: {}, solicitado: {}",
                            producto.getNombre(), producto.getStock(), detalle.getCantidad());
                    throw new StockInsuficienteException(producto.getNombre(), producto.getStock(),
                            detalle.getCantidad());
                }
            }
        }
        logger.debug("Verificación de stock completada exitosamente");
    }

    /**
     * Actualiza el inventario después de crear o modificar una factura
     */
    @Transactional
    public void actualizarInventario(List<DetalleFactura> detalles) {
        logger.debug("Actualizando inventario para {} detalles", detalles != null ? detalles.size() : 0);
        if (detalles == null)
            return;

        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                logger.debug("Actualizando stock para producto ID: {}, cantidad: -{}",
                        detalle.getProductoId(), detalle.getCantidad());
                Producto producto = productoService.actualizarStock(detalle.getProductoId(), -detalle.getCantidad());
                if (producto == null) {
                    logger.error("Error al actualizar stock para producto ID: {}", detalle.getProductoId());
                    throw new RuntimeException("Error al actualizar stock del producto: " + detalle.getProductoId());
                }
                logger.debug("Stock actualizado correctamente. Nuevo stock: {}", producto.getStock());
            }
        }
        logger.debug("Actualización de inventario completada exitosamente");
    }

    /**
     * Restaura el inventario cuando se anula una factura
     */
    @Transactional
    public void restaurarInventario(List<DetalleFactura> detalles) {
        logger.debug("Restaurando inventario para {} detalles", detalles != null ? detalles.size() : 0);
        if (detalles == null)
            return;

        for (DetalleFactura detalle : detalles) {
            if (detalle.getProductoId() != null) {
                logger.debug("Restaurando stock para producto ID: {}, cantidad: +{}",
                        detalle.getProductoId(), detalle.getCantidad());
                Producto producto = productoService.actualizarStock(detalle.getProductoId(), detalle.getCantidad());
                if (producto == null) {
                    logger.error("Error al restaurar stock para producto ID: {}", detalle.getProductoId());
                    throw new RuntimeException("Error al restaurar stock del producto: " + detalle.getProductoId());
                }
                logger.debug("Stock restaurado correctamente. Nuevo stock: {}", producto.getStock());
            }
        }
        logger.debug("Restauración de inventario completada exitosamente");
    }
}
