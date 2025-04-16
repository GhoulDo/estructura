package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.service.FacturacionUnificadaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturacion-unificada")
public class FacturacionUnificadaController {

    private final FacturacionUnificadaService facturacionUnificadaService;

    @Autowired
    public FacturacionUnificadaController(FacturacionUnificadaService facturacionUnificadaService) {
        this.facturacionUnificadaService = facturacionUnificadaService;
    }

    /**
     * Crea una factura a partir de una cita, permitiendo opcionalmente añadir productos
     * @param citaId ID de la cita a facturar
     * @param request Map con los IDs de productos y sus cantidades
     * @return La factura creada
     */
    @PostMapping("/facturar-cita/{citaId}")
    public ResponseEntity<Factura> facturarCita(
            @PathVariable String citaId,
            @RequestBody(required = false) Map<String, Object> request) {

        List<String> productosIds = request != null ? (List<String>) request.get("productosIds") : null;
        List<Integer> cantidades = request != null ? (List<Integer>) request.get("cantidades") : null;

        Factura factura = facturacionUnificadaService.facturarCitaConProductos(citaId, productosIds, cantidades);
        return ResponseEntity.ok(factura);
    }

    /**
     * Añade productos a una factura existente
     * @param facturaId ID de la factura
     * @param request Map con los IDs de productos y sus cantidades
     * @return La factura actualizada
     */
    @PutMapping("/agregar-productos/{facturaId}")
    public ResponseEntity<Factura> agregarProductos(
            @PathVariable String facturaId,
            @RequestBody Map<String, Object> request) {

        List<String> productosIds = (List<String>) request.get("productosIds");
        List<Integer> cantidades = (List<Integer>) request.get("cantidades");

        Factura factura = facturacionUnificadaService.agregarProductosAFactura(facturaId, productosIds, cantidades);
        return ResponseEntity.ok(factura);
    }

    /**
     * Marca una factura como pagada
     * @param facturaId ID de la factura
     * @return La factura actualizada
     */
    @PutMapping("/pagar/{facturaId}")
    public ResponseEntity<Factura> pagarFactura(@PathVariable String facturaId) {
        Factura factura = facturacionUnificadaService.pagarFactura(facturaId);
        return ResponseEntity.ok(factura);
    }

    /**
     * Obtiene todas las facturas de un cliente
     * @param clienteId ID del cliente
     * @return Lista de facturas del cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Factura>> obtenerFacturasCliente(@PathVariable String clienteId) {
        List<Factura> facturas = facturacionUnificadaService.obtenerFacturasCliente(clienteId);
        return ResponseEntity.ok(facturas);
    }
}
