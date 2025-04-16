// src/main/java/com/peluqueria/estructura/service/FacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ProductoService productoService;

    @Autowired
    public FacturaService(FacturaRepository facturaRepository, ProductoService productoService) {
        this.facturaRepository = facturaRepository;
        this.productoService = productoService;
    }

    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> findById(String id) {
        return facturaRepository.findById(id);
    }

    public List<Factura> findByClienteId(String clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }

    public List<Factura> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return facturaRepository.findByFechaBetween(inicio, fin);
    }

    public List<Factura> findByClienteUsuarioUsername(String username) {
        return facturaRepository.findByClienteUsuarioUsername(username);
    }

    public Factura save(Factura factura) {
        // Actualizamos el stock de productos si es necesario
        if (factura.getDetalles() != null) {
            factura.getDetalles().forEach(detalle -> {
                if (detalle.getProductoId() != null) {
                    // Reducimos el stock del producto
                    productoService.actualizarStock(detalle.getProductoId(), -detalle.getCantidad());
                }
            });
        }
        return facturaRepository.save(factura);
    }

    public void deleteById(String id) {
        facturaRepository.deleteById(id);
    }
}