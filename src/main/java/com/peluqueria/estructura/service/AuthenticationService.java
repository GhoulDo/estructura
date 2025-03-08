package com.peluqueria.estructura.service;
import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.UsuarioRepository;
import com.peluqueria.estructura.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request) {
        Optional<Usuario> usuario = usuarioRepository.findByUsername(request.getUsername());
        if (usuario.isPresent() && passwordEncoder.matches(request.getPassword(), usuario.get().getPassword())) {
            String token = jwtUtil.generateToken(usuario.get().getUsername()); // 🔹 Corrección aquí
            return new AuthResponse(token);
        }
        throw new RuntimeException("Credenciales inválidas");
    }

    public void register(RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());

        usuarioRepository.save(usuario);
    }
}
