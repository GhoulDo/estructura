// src/main/java/com/peluqueria/estructura/controller/FacturaController.java
package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.FacturaService;
import com.peluqueria.estructura.service.UsuarioService;
import com.peluqueria.estructura.service.ClienteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;

    public FacturaController(FacturaService facturaService, UsuarioService usuarioService, ClienteService clienteService) {
        this.facturaService = facturaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<Factura>> getAllFacturas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Si es ADMIN, devuelve todas las facturas
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.ok(facturaService.findAll());
        } 
        // Si es CLIENTE, solo devuelve sus facturas
        else {
            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
            
            if (usuarioOpt.isPresent()) {
                return clienteService.findByUsuarioId(usuarioOpt.get().getId())
                    .map(cliente -> ResponseEntity.ok(facturaService.findByClienteId(cliente.getId())))
                    .orElse(ResponseEntity.ok(List.of()));
            }
            
            return ResponseEntity.ok(List.of());
        }
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