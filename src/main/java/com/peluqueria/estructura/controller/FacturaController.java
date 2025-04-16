// src/main/java/com/peluqueria/estructura/controller/FacturaController.java
package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public ResponseEntity<List<Factura>> getAllFacturas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok(facturaService.findByClienteUsuarioUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getFacturaById(@PathVariable String id) {
        return facturaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Factura> createFactura(@RequestBody Factura factura) {
        return ResponseEntity.ok(facturaService.save(factura));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotalFactura(@PathVariable String id) {
        return facturaService.findById(id)
                .map(factura -> ResponseEntity.ok(factura.getTotal()))
                .orElse(ResponseEntity.notFound().build());
    }
}