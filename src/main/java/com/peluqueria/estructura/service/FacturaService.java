// src/main/java/com/peluqueria/estructura/service/FacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacturaService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);
    private final FacturaRepository facturaRepository;

    @Autowired
    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
        logger.info("FacturaService inicializado correctamente");
    }

    public List<Factura> findAll() {
        logger.debug("Solicitando todas las facturas");
        return facturaRepository.findAll();
    }

    public Optional<Factura> findById(String id) {
        logger.debug("Buscando factura por ID: {}", id);
        return facturaRepository.findById(id);
    }

    public List<Factura> findByClienteId(String clienteId) {
        logger.debug("Buscando facturas para clienteId: {}", clienteId);
        return facturaRepository.findByClienteId(clienteId);
    }

    public Factura save(Factura factura) {
        if (factura.getId() == null) {
            factura.setId(UUID.randomUUID().toString());
            factura.setFecha(LocalDateTime.now());
            logger.debug("Creando nueva factura con ID generado: {}", factura.getId());
        } else {
            logger.debug("Actualizando factura existente con ID: {}", factura.getId());
        }
        return facturaRepository.save(factura);
    }

    public void deleteById(String id) {
        logger.debug("Eliminando factura con ID: {}", id);
        facturaRepository.deleteById(id);
    }

    public Factura actualizarEstadoFactura(String facturaId, String nuevoEstado) {
        logger.debug("Cambiando estado de factura {} a {}", facturaId, nuevoEstado);
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));

        factura.setEstado(nuevoEstado);
        return facturaRepository.save(factura);
    }

    public List<Factura> findByClienteUsuarioUsername(String username) {
        try {
            logger.debug("Buscando facturas para usuario: {}", username);
            return facturaRepository.findByClienteUsuarioUsername(username);
        } catch (Exception e) {
            logger.error("Error al buscar facturas por username {}: {}", username, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}