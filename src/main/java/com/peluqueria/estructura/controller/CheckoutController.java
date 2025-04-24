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
        logger.info("CheckoutController inicializado correctamente");
    }

    /**
     * Endpoint para diagnóstico del controlador
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<?> diagnosticarServicio(Authentication auth) {
        logger.info("Ejecutando diagnóstico del controlador de checkout");
        Map<String, Object> diagnostico = new HashMap<>();

        try {
            // Información básica
            diagnostico.put("estado", "controlador operativo");
            diagnostico.put("timestamp", System.currentTimeMillis());
            diagnostico.put("servicio_checkout", checkoutService != null ? "inicializado" : "null");

            // Información de autenticación
            if (auth != null) {
                diagnostico.put("autenticacion", Map.of(
                        "nombre", auth.getName(),
                        "autoridades", auth.getAuthorities(),
                        "autenticado", auth.isAuthenticated()));
            } else {
                diagnostico.put("autenticacion", "No autenticado");
            }

            logger.info("Diagnóstico completado con éxito");
            return ResponseEntity.ok(diagnostico);

        } catch (Exception e) {
            logger.error("Error en diagnóstico: {}", e.getMessage(), e);
            diagnostico.put("error", "Error en diagnóstico");
            diagnostico.put("mensaje", e.getMessage());
            diagnostico.put("stacktrace", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(diagnostico);
        }
    }

    /**
     * Endpoint para obtener resumen de checkout
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(Authentication auth) {
        logger.info("GET /api/checkout/resumen - Usuario: {}", auth != null ? auth.getName() : "no autenticado");

        // Verificar autenticación
        if (auth == null) {
            logger.error("Error de autenticación: El usuario no está autenticado");
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "No autenticado");
            error.put("error", "Se requiere autenticación para esta operación");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            CheckoutResumenDTO resumen = checkoutService.obtenerResumen(auth);
            logger.info("Resumen generado exitosamente para {}", auth.getName());
            return ResponseEntity.ok(resumen);
        } catch (ValidationException e) {
            logger.warn("Error de validación en checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error de validación");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado en checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Recurso no encontrado");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (StockInsuficienteException e) {
            logger.warn("Stock insuficiente en checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Stock insuficiente");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error al obtener resumen de checkout: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error al procesar el resumen de checkout");
            error.put("error", e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para confirmar el checkout
     */
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCheckout(
            Authentication auth,
            @RequestBody(required = false) Map<String, String> checkoutInfo) {

        logger.info("POST /api/checkout/confirmar - Usuario: {}", auth != null ? auth.getName() : "no autenticado");

        // Verificar autenticación
        if (auth == null) {
            logger.error("Error de autenticación: El usuario no está autenticado");
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "No autenticado");
            error.put("error", "Se requiere autenticación para esta operación");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (checkoutInfo == null) {
            checkoutInfo = new HashMap<>();
        }

        try {
            Factura factura = checkoutService.confirmarCheckout(auth, checkoutInfo);
            logger.info("Checkout confirmado exitosamente para {}, factura ID: {}", auth.getName(), factura.getId());
            return ResponseEntity.ok(factura);
        } catch (ValidationException e) {
            logger.warn("Error de validación en confirmar checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error de validación");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado en confirmar checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Recurso no encontrado");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (StockInsuficienteException e) {
            logger.warn("Stock insuficiente en confirmar checkout: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Stock insuficiente");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error al confirmar checkout: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "Error al confirmar el checkout");
            error.put("error", e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
