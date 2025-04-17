// src/main/java/com/peluqueria/estructura/service/FacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    @Autowired
    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
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

    public Factura save(Factura factura) {
        if (factura.getId() == null) {
            factura.setId(UUID.randomUUID().toString());
            factura.setFecha(LocalDateTime.now());
        }
        return facturaRepository.save(factura);
    }

    public void deleteById(String id) {
        facturaRepository.deleteById(id);
    }

    public Factura actualizarEstadoFactura(String facturaId, String nuevoEstado) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));

        factura.setEstado(nuevoEstado);
        return facturaRepository.save(factura);
    }

    public List<Factura> findByClienteUsuarioUsername(String username) {
        return facturaRepository.findByClienteUsuarioUsername(username);
    }
}