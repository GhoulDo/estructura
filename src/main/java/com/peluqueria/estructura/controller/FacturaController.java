// src/main/java/com/peluqueria/estructura/controller/FacturaController.java
package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.FacturaService;
import com.peluqueria.estructura.service.UsuarioService;
import com.peluqueria.estructura.service.ClienteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);
    private final FacturaService facturaService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;

    public FacturaController(FacturaService facturaService, UsuarioService usuarioService,
            ClienteService clienteService) {
        this.facturaService = facturaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        logger.info("FacturaController inicializado correctamente");
    }

    @GetMapping
    public ResponseEntity<List<Factura>> getAllFacturas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Obteniendo facturas para usuario: {}", auth.getName());

        // Si es ADMIN, devuelve todas las facturas
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            logger.debug("Usuario con rol ADMIN, retornando todas las facturas");
            return ResponseEntity.ok(facturaService.findAll());
        }
        // Si es CLIENTE, solo devuelve sus facturas
        else {
            logger.debug("Usuario con rol CLIENTE, buscando sus facturas específicas");
            return ResponseEntity.ok(facturaService.findByClienteUsuarioUsername(auth.getName()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getFacturaById(@PathVariable String id) {
        logger.debug("Buscando factura con ID: {}", id);
        return facturaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Factura> createFactura(@RequestBody Factura factura) {
        logger.debug("Creando nueva factura");
        return ResponseEntity.ok(facturaService.save(factura));
    }

    @GetMapping("/cliente")
    public ResponseEntity<List<Factura>> getFacturasCliente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.debug("Obteniendo facturas para cliente con username: {}", username);

        try {
            List<Factura> facturas = facturaService.findByClienteUsuarioUsername(username);
            logger.debug("Encontradas {} facturas para el cliente {}", facturas.size(), username);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            logger.error("Error al buscar facturas del cliente {}: {}", username, e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Factura>> getFacturasByClienteId(@PathVariable String clienteId) {
        logger.debug("Obteniendo facturas para clienteId: {}", clienteId);
        List<Factura> facturas = facturaService.findByClienteId(clienteId);
        logger.debug("Encontradas {} facturas para el cliente ID {}", facturas.size(), clienteId);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotalFactura(@PathVariable String id) {
        return facturaService.findById(id)
                .map(factura -> ResponseEntity.ok(factura.getTotal()))
                .orElse(ResponseEntity.notFound().build());
    }
}