// src/main/java/com/peluqueria/estructura/controller/FacturaController.java
package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.FacturaDTO;
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
    public ResponseEntity<List<FacturaDTO>> getAllFacturas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(facturaService.getAllFacturas(auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> getFacturaById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(facturaService.getFacturaById(id, auth));
    }

    @PostMapping
    public ResponseEntity<FacturaDTO> createFactura(@RequestBody FacturaDTO facturaDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(facturaService.createFactura(facturaDTO, auth));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotalFactura(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(facturaService.calcularTotalFactura(id, auth));
    }
}