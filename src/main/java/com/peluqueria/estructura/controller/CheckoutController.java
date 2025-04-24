package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Endpoint para obtener el resumen del checkout
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(Authentication auth) {
        logger.info("GET /api/checkout/resumen - Usuario: {}", auth.getName());

        try {
            CheckoutResumenDTO resumen = checkoutService.obtenerResumen(auth);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            logger.error("Error al obtener resumen de checkout: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al obtener el resumen de checkout");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para confirmar el checkout y crear la factura
     */
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCheckout(
            Authentication auth,
            @RequestBody(required = false) Map<String, String> checkoutInfo) {

        logger.info("POST /api/checkout/confirmar - Usuario: {}", auth.getName());

        if (checkoutInfo == null) {
            checkoutInfo = new HashMap<>();
        }

        try {
            Factura factura = checkoutService.confirmarCheckout(auth, checkoutInfo);
            return ResponseEntity.ok(factura);
        } catch (Exception e) {
            logger.error("Error al confirmar checkout: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al confirmar la compra");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para diagnosticar el estado del controlador
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnosticar(Authentication auth) {
        Map<String, Object> diagnostico = new HashMap<>();
        diagnostico.put("controlador", "CheckoutController");
        diagnostico.put("status", "operativo");
        diagnostico.put("usuario", auth.getName());
        diagnostico.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(diagnostico);
    }
}
