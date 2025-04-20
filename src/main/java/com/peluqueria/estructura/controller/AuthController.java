package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.service.AuthenticationService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest loginRequest) {
        try {
            AuthResponse response = authenticationService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de autenticación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales inválidas: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error interno en el login: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            authenticationService.register(registerRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario registrado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(Authentication authentication) {
        try {
            String username = authentication.getName();
            authenticationService.invalidateToken(username);
            return ResponseEntity.ok("Logout exitoso. Token invalidado.");
        } catch (Exception e) {
            logger.error("Error durante el logout: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}
