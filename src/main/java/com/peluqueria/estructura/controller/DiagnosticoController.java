package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;
import com.peluqueria.estructura.service.ClienteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diagnostico")
public class DiagnosticoController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticoController.class);
    private final FacturaRepository facturaRepository;
    private final ClienteService clienteService;

    @Autowired
    public DiagnosticoController(FacturaRepository facturaRepository, ClienteService clienteService) {
        this.facturaRepository = facturaRepository;
        this.clienteService = clienteService;
    }

    @GetMapping("/facturas")
    public ResponseEntity<?> diagnosticarFacturas(Authentication auth) {
        logger.info("Realizando diagnóstico de facturas para usuario: {}", auth.getName());

        Map<String, Object> diagnostico = new HashMap<>();
        try {
            // 1. Obtener todas las facturas
            List<Factura> todasFacturas = facturaRepository.findAll();
            diagnostico.put("total_facturas", todasFacturas.size());

            // 2. Información resumida de cada factura
            List<Map<String, Object>> infoFacturas = todasFacturas.stream()
                    .map(factura -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", factura.getId());
                        info.put("fecha", factura.getFecha());
                        info.put("total", factura.getTotal());
                        info.put("estado", factura.getEstado());

                        // Información del cliente
                        if (factura.getCliente() != null) {
                            Map<String, Object> clienteInfo = new HashMap<>();
                            clienteInfo.put("id", factura.getCliente().getId());
                            clienteInfo.put("nombre", factura.getCliente().getNombre());
                            clienteInfo.put("tipo_referencia", factura.getCliente().getClass().getName());

                            info.put("cliente", clienteInfo);
                        } else {
                            info.put("cliente", null);
                        }

                        // Detalles
                        info.put("detalles_count", factura.getDetalles() != null ? factura.getDetalles().size() : 0);

                        return info;
                    })
                    .collect(Collectors.toList());

            diagnostico.put("facturas", infoFacturas);

            // 3. Diagnóstico específico para el usuario actual
            if (auth != null) {
                try {
                    Cliente cliente = clienteService.findByUsuarioUsername(auth.getName());
                    String clienteId = cliente.getId();

                    Map<String, Object> diagnosticoUsuario = new HashMap<>();
                    diagnosticoUsuario.put("username", auth.getName());
                    diagnosticoUsuario.put("clienteId", clienteId);

                    // Probar diferentes métodos de búsqueda
                    List<Factura> porClienteRef = facturaRepository.findByClienteRef(clienteId);
                    diagnosticoUsuario.put("facturas_por_clienteRef", porClienteRef.size());

                    List<Factura> porClienteId = facturaRepository.findByClienteId(clienteId);
                    diagnosticoUsuario.put("facturas_por_clienteId", porClienteId.size());

                    List<Factura> porUsername = facturaRepository.findByClienteUsuarioUsername(auth.getName());
                    diagnosticoUsuario.put("facturas_por_username", porUsername.size());

                    diagnostico.put("diagnostico_usuario", diagnosticoUsuario);
                } catch (Exception e) {
                    diagnostico.put("error_diagnostico_usuario", e.getMessage());
                }
            }

            return ResponseEntity.ok(diagnostico);
        } catch (Exception e) {
            logger.error("Error en diagnóstico de facturas: {}", e.getMessage(), e);
            diagnostico.put("error", e.getMessage());
            return ResponseEntity.status(500).body(diagnostico);
        }
    }

    @GetMapping("/facturas/cliente")
    public ResponseEntity<?> diagnosticarFacturasCliente(Authentication auth) {
        Map<String, Object> diagnostico = new HashMap<>();
        try {
            Cliente cliente = clienteService.findByUsuarioUsername(auth.getName());

            diagnostico.put("username", auth.getName());
            diagnostico.put("clienteId", cliente.getId());
            diagnostico.put("clienteNombre", cliente.getNombre());

            // Probar diferentes consultas
            List<Factura> porRef = facturaRepository.findByClienteRef(cliente.getId());
            diagnostico.put("facturas_por_clienteRef", porRef.size());
            if (!porRef.isEmpty()) {
                diagnostico.put("primera_factura_ref", porRef.get(0).getId());
            }

            List<Factura> porId = facturaRepository.findByClienteId(cliente.getId());
            diagnostico.put("facturas_por_clienteId", porId.size());
            if (!porId.isEmpty()) {
                diagnostico.put("primera_factura_id", porId.get(0).getId());
            }

            return ResponseEntity.ok(diagnostico);
        } catch (Exception e) {
            diagnostico.put("error", e.getMessage());
            return ResponseEntity.status(500).body(diagnostico);
        }
    }
}
