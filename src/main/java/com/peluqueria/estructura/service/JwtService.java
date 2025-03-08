package com.peluqueria.estructura.service;



import com.peluqueria.estructura.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtUtil jwtUtil;

    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(String username) { // 🔹 Corrección aquí
        return jwtUtil.generateToken(username);
    }

    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    public boolean validateToken(String token, String username) { // 🔹 Corrección aquí
        return jwtUtil.validateToken(token, username);
    }
}

