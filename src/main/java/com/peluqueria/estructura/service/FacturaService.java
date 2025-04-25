// src/main/java/com/peluqueria/estructura/service/FacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacturaService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);
    private final FacturaRepository facturaRepository;
    private final ClienteService clienteService; // Añadido la dependencia faltante

    @Autowired
    public FacturaService(FacturaRepository facturaRepository, ClienteService clienteService) {
        this.facturaRepository = facturaRepository;
        this.clienteService = clienteService; // Inicializado clienteService
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

    /**
     * Busca todas las facturas asociadas a un usuario, usando múltiples estrategias
     * de búsqueda
     */
    public List<Factura> findAllFacturasForUser(String username) {
        logger.debug("Buscando todas las facturas para el usuario: {}", username);

        try {
            // Primero, encuentra al cliente asociado al usuario
            Cliente cliente = clienteService.findByUsuarioUsername(username);
            String clienteId = cliente.getId();

            // Lista para acumular resultados
            List<Factura> facturasTotales = new ArrayList<>();

            // Estrategia 1: Buscar por username directamente
            logger.debug("Estrategia 1: Buscando por username: {}", username);
            List<Factura> facturasByUsername = facturaRepository.findByClienteUsuarioUsername(username);
            logger.debug("Encontradas {} facturas por username", facturasByUsername.size());
            facturasTotales.addAll(facturasByUsername);

            // Estrategia 2: Buscar por ID de cliente
            logger.debug("Estrategia 2: Buscando por ID de cliente: {}", clienteId);
            List<Factura> facturasByClienteId = facturaRepository.findByClienteId(clienteId);
            logger.debug("Encontradas {} facturas por ID de cliente", facturasByClienteId.size());

            // Añadir solo facturas que no estén ya en la lista
            facturasByClienteId.forEach(factura -> {
                if (!contieneFactura(facturasTotales, factura.getId())) {
                    facturasTotales.add(factura);
                }
            });

            // Estrategia 3: Búsqueda combinada con cliente ID o username
            logger.debug("Estrategia 3: Búsqueda combinada para {}", username);
            List<Factura> facturasCombinadas = facturaRepository.findByClienteIdOrUsername(username);
            logger.debug("Encontradas {} facturas por búsqueda combinada", facturasCombinadas.size());

            facturasCombinadas.forEach(factura -> {
                if (!contieneFactura(facturasTotales, factura.getId())) {
                    facturasTotales.add(factura);
                }
            });

            // Estrategia 4: Búsqueda flexible para tolerar diferentes estructuras
            logger.debug("Estrategia 4: Búsqueda flexible con clienteId: {} y username: {}", clienteId, username);
            List<Factura> facturasFlex = facturaRepository.findAllClientFacturas(clienteId, username);
            logger.debug("Encontradas {} facturas por búsqueda flexible", facturasFlex.size());

            facturasFlex.forEach(factura -> {
                if (!contieneFactura(facturasTotales, factura.getId())) {
                    facturasTotales.add(factura);
                }
            });

            logger.debug("Total de facturas encontradas combinando todas las estrategias: {}", facturasTotales.size());
            return facturasTotales;

        } catch (Exception e) {
            logger.error("Error buscando facturas para usuario {}: {}", username, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean contieneFactura(List<Factura> facturas, String id) {
        return facturas.stream().anyMatch(f -> f.getId().equals(id));
    }
}