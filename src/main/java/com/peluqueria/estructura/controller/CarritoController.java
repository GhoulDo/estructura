package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import com.peluqueria.estructura.service.CarritoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public CarritoController(CarritoService carritoService, UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository) {
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Obtiene el contenido del carrito del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> getCarrito() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("GET /api/carrito - Usuario: {}, Auth principal: {}, Auth details: {}",
                auth.getName(), auth.getPrincipal(), auth.getDetails());

        try {
            Carrito carrito = carritoService.getCarrito(auth);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            logger.error("Error al obtener carrito: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al obtener el carrito");
            response.put("error", e.getMessage());

            // Para diagnóstico, incluir información sobre la autenticación
            response.put("auth_info", Map.of(
                    "username", auth.getName(),
                    "authorities", auth.getAuthorities().toString(),
                    "authenticated", auth.isAuthenticated()));

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

    /**
     * Endpoint de diagnóstico para ayudar a resolver problemas de autenticación
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<?> diagnosticarAutenticacion(Authentication auth) {
        Map<String, Object> diagnostico = new HashMap<>();

        try {
            diagnostico.put("auth_recibida", auth != null);

            if (auth != null) {
                diagnostico.put("nombre_usuario", auth.getName());
                diagnostico.put("autoridades", auth.getAuthorities());
                diagnostico.put("autenticado", auth.isAuthenticated());
                diagnostico.put("detalles", auth.getDetails());
                diagnostico.put("principal_tipo", auth.getPrincipal().getClass().getName());
            } else {
                diagnostico.put("error", "No hay autenticación");
            }

            return ResponseEntity.ok(diagnostico);
        } catch (Exception e) {
            diagnostico.put("error", "Error al procesar diagnóstico: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(diagnostico);
        }
    }

    /**
     * Endpoint de diagnóstico para verificar los datos de autenticación y usuario
     */
    @GetMapping("/debug")
    public ResponseEntity<?> diagnosticar(Authentication auth) {
        Map<String, Object> info = new HashMap<>();

        try {
            // Información de autenticación
            info.put("auth_name", auth.getName());
            info.put("auth_principal", auth.getPrincipal());
            info.put("auth_authorities", auth.getAuthorities());

            // Obtener todos los usuarios
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<Map<String, Object>> usuariosInfo = new ArrayList<>();

            for (Usuario u : usuarios) {
                Map<String, Object> usuarioInfo = new HashMap<>();
                usuarioInfo.put("id", u.getId());
                usuarioInfo.put("username", u.getUsername());
                // En lugar de llamar a getRoles(), que parece no existir, podemos usar otro
                // enfoque
                usuariosInfo.add(usuarioInfo);
            }

            info.put("usuarios_en_db", usuariosInfo);

            // Obtener todos los clientes
            List<Cliente> clientes = clienteRepository.findAll();
            List<Map<String, Object>> clientesInfo = new ArrayList<>();

            for (Cliente c : clientes) {
                Map<String, Object> clienteInfo = new HashMap<>();
                clienteInfo.put("id", c.getId());
                clienteInfo.put("nombre", c.getNombre());
                clienteInfo.put("usuario_id", c.getUsuario() != null ? c.getUsuario().getId() : null);
                clienteInfo.put("usuario_username", c.getUsuario() != null ? c.getUsuario().getUsername() : null);
                clientesInfo.add(clienteInfo);
            }

            info.put("clientes_en_db", clientesInfo);

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            logger.error("Error en diagnóstico: {}", e.getMessage(), e);
            info.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(info);
        }
    }
}
