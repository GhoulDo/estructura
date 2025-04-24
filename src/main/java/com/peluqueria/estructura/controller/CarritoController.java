package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.service.CarritoService;
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
@RequestMapping("/api/carrito")
public class CarritoController {

    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);
    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    /**
     * Obtiene el contenido del carrito del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> getCarrito(Authentication auth) {
        logger.info("GET /api/carrito - Usuario: {}", auth.getName());
        try {
            Carrito carrito = carritoService.getCarrito(auth);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            logger.error("Error al obtener carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al obtener el carrito");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Agrega un producto al carrito
     */
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarProducto(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        logger.info("POST /api/carrito/agregar - Usuario: {}, Datos: {}", auth.getName(), request);

        try {
            String productoId = request.get("productoId").toString();
            int cantidad = Integer.parseInt(request.get("cantidad").toString());

            // Validaciones básicas
            if (productoId == null || productoId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID de producto no especificado"));
            }

            if (cantidad <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "La cantidad debe ser mayor a cero"));
            }

            Carrito carrito = carritoService.agregarProducto(auth, productoId, cantidad);
            return ResponseEntity.ok(carrito);
        } catch (NumberFormatException e) {
            logger.error("Error de formato en la cantidad: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de cantidad inválido"));
        } catch (Exception e) {
            logger.error("Error al agregar producto al carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al agregar producto al carrito");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarCantidad(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        logger.info("PUT /api/carrito/actualizar - Usuario: {}", auth.getName());

        try {
            String productoId = request.get("productoId").toString();
            int cantidad = Integer.parseInt(request.get("cantidad").toString());

            if (cantidad < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "La cantidad no puede ser negativa"));
            }

            Carrito carrito = carritoService.actualizarCantidad(auth, productoId, cantidad);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            logger.error("Error al actualizar cantidad en carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al actualizar cantidad en el carrito");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Elimina un producto del carrito
     */
    @DeleteMapping("/eliminar/{productoId}")
    public ResponseEntity<?> eliminarProducto(
            Authentication auth,
            @PathVariable String productoId) {

        logger.info("DELETE /api/carrito/eliminar/{} - Usuario: {}", productoId, auth.getName());

        try {
            Carrito carrito = carritoService.eliminarProducto(auth, productoId);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            logger.error("Error al eliminar producto del carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al eliminar producto del carrito");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Vacía el carrito
     */
    @DeleteMapping("/vaciar")
    public ResponseEntity<?> vaciarCarrito(Authentication auth) {
        logger.info("DELETE /api/carrito/vaciar - Usuario: {}", auth.getName());

        try {
            carritoService.vaciarCarrito(auth);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al vaciar carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al vaciar el carrito");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
