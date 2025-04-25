package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public DiagnosticoController(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    /**
     * Endpoint para diagnosticar las facturas existentes y su estructura
     */
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

                        // Datos básicos de la factura
                        info.put("id", factura.getId());
                        info.put("fecha", factura.getFecha());
                        info.put("total", factura.getTotal());
                        info.put("estado", factura.getEstado());

                        // Información del cliente
                        if (factura.getCliente() != null) {
                            Map<String, Object> clienteInfo = new HashMap<>();
                            clienteInfo.put("id", factura.getCliente().getId());
                            clienteInfo.put("nombre", factura.getCliente().getNombre());

                            // Información del usuario asociado al cliente
                            if (factura.getCliente().getUsuario() != null) {
                                Map<String, Object> usuarioInfo = new HashMap<>();
                                usuarioInfo.put("id", factura.getCliente().getUsuario().getId());
                                usuarioInfo.put("username", factura.getCliente().getUsuario().getUsername());
                                usuarioInfo.put("email", factura.getCliente().getUsuario().getEmail());

                                clienteInfo.put("usuario", usuarioInfo);
                            } else {
                                clienteInfo.put("usuario", null);
                            }

                            info.put("cliente", clienteInfo);
                        } else {
                            info.put("cliente", null);
                        }

                        // Información de los detalles
                        if (factura.getDetalles() != null) {
                            info.put("detalles_count", factura.getDetalles().size());
                            info.put("tiene_productos",
                                    factura.getDetalles().stream().anyMatch(d -> d.getProductoId() != null));
                            info.put("tiene_servicios",
                                    factura.getDetalles().stream().anyMatch(d -> d.getServicioId() != null));

                            // Totales por tipo
                            long cantidadProductos = factura.getDetalles().stream()
                                    .filter(d -> d.getProductoId() != null).count();
                            long cantidadServicios = factura.getDetalles().stream()
                                    .filter(d -> d.getServicioId() != null).count();

                            info.put("cantidad_productos", cantidadProductos);
                            info.put("cantidad_servicios", cantidadServicios);
                        } else {
                            info.put("detalles_count", 0);
                            info.put("tiene_productos", false);
                            info.put("tiene_servicios", false);
                        }

                        return info;
                    })
                    .collect(Collectors.toList());

            diagnostico.put("facturas", infoFacturas);

            // 3. Estadísticas generales
            long facturasConDetalles = todasFacturas.stream()
                    .filter(f -> f.getDetalles() != null && !f.getDetalles().isEmpty())
                    .count();

            long facturasPendientes = todasFacturas.stream()
                    .filter(f -> "PENDIENTE".equals(f.getEstado()))
                    .count();

            long facturasPagadas = todasFacturas.stream()
                    .filter(f -> "PAGADA".equals(f.getEstado()))
                    .count();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("facturas_con_detalles", facturasConDetalles);
            estadisticas.put("facturas_pendientes", facturasPendientes);
            estadisticas.put("facturas_pagadas", facturasPagadas);

            diagnostico.put("estadisticas", estadisticas);

            logger.info("Diagnóstico completado. Encontradas {} facturas", todasFacturas.size());
            return ResponseEntity.ok(diagnostico);

        } catch (Exception e) {
            logger.error("Error durante el diagnóstico de facturas: {}", e.getMessage(), e);

            diagnostico.put("error", true);
            diagnostico.put("mensaje", "Error al realizar el diagnóstico: " + e.getMessage());
            diagnostico.put("tipo_error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(diagnostico);
        }
    }
}
