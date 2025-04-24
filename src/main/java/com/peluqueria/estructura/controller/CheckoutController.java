package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
import com.peluqueria.estructura.exception.StockInsuficienteException;
import com.peluqueria.estructura.exception.ValidationException;
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
        logger.info("CheckoutController inicializado correctamente");
    }

    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnostico(Authentication auth) {
        Map<String, Object> diagnostico = new HashMap<>();
        diagnostico.put("controlador", "CheckoutController");
        diagnostico.put("status", "operativo");
        diagnostico.put("usuario", auth != null ? auth.getName() : "no autenticado");
        diagnostico.put("timestamp", System.currentTimeMillis());
        logger.info("Diagnóstico solicitado por usuario: {}", auth != null ? auth.getName() : "anónimo");
        return ResponseEntity.ok(diagnostico);
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(Authentication auth) {
        logger.info("GET /api/checkout/resumen - Usuario: {}", auth.getName());

        try {
            CheckoutResumenDTO resumen = checkoutService.obtenerResumen(auth);
            return ResponseEntity.ok(resumen);
        } catch (ResourceNotFoundException e) {
            logger.error("Recurso no encontrado: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Recurso no encontrado");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (StockInsuficienteException e) {
            logger.error("Stock insuficiente: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Stock insuficiente");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ValidationException e) {
            logger.error("Error de validación: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener resumen: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error interno del servidor");
            response.put("mensaje", "Error al obtener el resumen de checkout");
            response.put("detalleError", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

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
        } catch (ResourceNotFoundException e) {
            logger.error("Recurso no encontrado: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Recurso no encontrado");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (StockInsuficienteException e) {
            logger.error("Stock insuficiente: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Stock insuficiente");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ValidationException e) {
            logger.error("Error de validación: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error inesperado al confirmar checkout: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error interno del servidor");
            response.put("mensaje", "Error al confirmar la compra");
            response.put("detalleError", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
