package com.peluqueria.estructura.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAuthInfo(Authentication authentication) {
        Map<String, Object> authInfo = new HashMap<>();

        if (authentication != null) {
            authInfo.put("isAuthenticated", authentication.isAuthenticated());
            authInfo.put("username", authentication.getName());
            authInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            authInfo.put("isAuthenticated", false);
        }

        return ResponseEntity.ok(authInfo);
    }

    @GetMapping("/public")
    public ResponseEntity<String> getPublicInfo() {
        return ResponseEntity.ok("Esta es una ruta pública para verificar que el API está funcionando correctamente.");
    }
}
