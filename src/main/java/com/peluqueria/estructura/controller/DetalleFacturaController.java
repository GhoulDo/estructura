// src/main/java/com/peluqueria/estructura/controller/DetalleFacturaController.java
package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.service.DetalleFacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas/{facturaId}/detalles")
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService) {
        this.detalleFacturaService = detalleFacturaService;
    }

    @GetMapping
    public ResponseEntity<List<DetalleFacturaDTO>> getAllDetalles(@PathVariable String facturaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(detalleFacturaService.getAllDetalles(facturaId, auth));
    }

    @GetMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> getDetalleById(@PathVariable String facturaId, @PathVariable String detalleId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(detalleFacturaService.getDetalleById(facturaId, detalleId, auth));
    }

    @PostMapping
    public ResponseEntity<DetalleFacturaDTO> createDetalle(@PathVariable String facturaId, @RequestBody DetalleFacturaDTO detalleDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(detalleFacturaService.createDetalle(facturaId, detalleDTO, auth));
    }

    @PutMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> updateDetalle(
            @PathVariable String facturaId, 
            @PathVariable String detalleId, 
            @RequestBody DetalleFacturaDTO detalleDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(detalleFacturaService.updateDetalle(facturaId, detalleId, detalleDTO, auth));
    }

    @DeleteMapping("/{detalleId}")
    public ResponseEntity<Void> deleteDetalle(@PathVariable String facturaId, @PathVariable String detalleId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        detalleFacturaService.deleteDetalle(facturaId, detalleId, auth);
        return ResponseEntity.noContent().build();
    }
}