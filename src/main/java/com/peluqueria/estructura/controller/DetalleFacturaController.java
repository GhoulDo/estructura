package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.service.DetalleFacturaService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<DetalleFacturaDTO>> getAllDetalles(@PathVariable Long facturaId) {
        return ResponseEntity.ok(detalleFacturaService.getAllDetalles(facturaId));
    }

    @GetMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> getDetalleById(@PathVariable Long facturaId, @PathVariable Long detalleId) {
        return ResponseEntity.ok(detalleFacturaService.getDetalleById(facturaId, detalleId));
    }

    @PostMapping
    public ResponseEntity<DetalleFacturaDTO> createDetalle(@PathVariable Long facturaId, @RequestBody DetalleFacturaDTO detalleDTO) {
        return ResponseEntity.ok(detalleFacturaService.createDetalle(facturaId, detalleDTO));
    }

    @PutMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> updateDetalle(@PathVariable Long facturaId, @PathVariable Long detalleId, @RequestBody DetalleFacturaDTO detalleDTO) {
        return ResponseEntity.ok(detalleFacturaService.updateDetalle(facturaId, detalleId, detalleDTO));
    }

    @DeleteMapping("/{detalleId}")
    public ResponseEntity<Void> deleteDetalle(@PathVariable Long facturaId, @PathVariable Long detalleId) {
        detalleFacturaService.deleteDetalle(facturaId, detalleId);
        return ResponseEntity.noContent().build();
    }
}
