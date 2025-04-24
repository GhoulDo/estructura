package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
import com.peluqueria.estructura.exception.ValidationException;
import com.peluqueria.estructura.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnostico() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "Controlador de checkout funcionando correctamente");
        info.put("timestamp", System.currentTimeMillis());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        info.put("authenticated", auth != null && auth.isAuthenticated());
        info.put("username", auth != null ? auth.getName() : "no autenticado");

        return ResponseEntity.ok(info);
    }

    @GetMapping("/resumen")
    public ResponseEntity<CheckoutResumenDTO> obtenerResumen(Authentication auth) {
        logger.info("GET /api/checkout/resumen - Usuario: {}", auth.getName());

        try {
            CheckoutResumenDTO resumen = checkoutService.obtenerResumen(auth);
            return ResponseEntity.ok(resumen);
        } catch (ResourceNotFoundException e) {
            logger.error("Recurso no encontrado: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            logger.error("Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener resumen de checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener el resumen de checkout: " + e.getMessage());
        }
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Factura> confirmarCheckout(
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
            throw e;
        } catch (ValidationException e) {
            logger.error("Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error al confirmar checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al confirmar la compra: " + e.getMessage());
        }
    }
}
