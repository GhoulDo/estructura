package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping("/auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAuthInfo(Authentication authentication) {
        Map<String, Object> authInfo = new HashMap<>();

        if (authentication != null) {
            authInfo.put("isAuthenticated", authentication.isAuthenticated());
            authInfo.put("username", authentication.getName());
            authInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            authInfo.put("isAuthenticated", false);
        }

        return ResponseEntity.ok(authInfo);
    }

    @GetMapping("/public")
    public ResponseEntity<String> getPublicInfo() {
        return ResponseEntity.ok("Esta es una ruta pública para verificar que el API está funcionando correctamente.");
    }

    @GetMapping("/facturas-diagnostico")
    public ResponseEntity<?> diagnosticarFacturas(Authentication auth) {
        Map<String, Object> diagnostico = new HashMap<>();

        try {
            // Obtener todas las facturas
            List<Factura> todasLasFacturas = facturaRepository.findAll();
            diagnostico.put("total_facturas", todasLasFacturas.size());

            // Información resumida de las facturas
            List<Map<String, Object>> facturasInfo = todasLasFacturas.stream()
                    .map(f -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", f.getId());
                        info.put("fecha", f.getFecha());
                        info.put("total", f.getTotal());
                        info.put("estado", f.getEstado());

                        Map<String, Object> clienteInfo = new HashMap<>();
                        if (f.getCliente() != null) {
                            clienteInfo.put("id", f.getCliente().getId());
                            clienteInfo.put("nombre", f.getCliente().getNombre());

                            if (f.getCliente().getUsuario() != null) {
                                Map<String, Object> usuarioInfo = new HashMap<>();
                                usuarioInfo.put("id", f.getCliente().getUsuario().getId());
                                usuarioInfo.put("username", f.getCliente().getUsuario().getUsername());
                                usuarioInfo.put("email", f.getCliente().getUsuario().getEmail());
                                clienteInfo.put("usuario", usuarioInfo);
                            } else {
                                clienteInfo.put("usuario", "null");
                            }
                        } else {
                            clienteInfo.put("cliente", "null");
                        }

                        info.put("cliente", clienteInfo);
                        info.put("detalles_count", f.getDetalles() != null ? f.getDetalles().size() : 0);

                        return info;
                    })
                    .collect(Collectors.toList());

            diagnostico.put("facturas", facturasInfo);

            return ResponseEntity.ok(diagnostico);
        } catch (Exception e) {
            logger.error("Error en diagnóstico de facturas: {}", e.getMessage(), e);
            diagnostico.put("error", e.getMessage());
            return ResponseEntity.status(500).body(diagnostico);
        }
    }
}
