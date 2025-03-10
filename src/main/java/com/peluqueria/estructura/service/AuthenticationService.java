package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.UsuarioRepository;
import com.peluqueria.estructura.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UsuarioRepository usuarioRepository, 
            JwtUtil jwtUtil, 
            PasswordEncoder passwordEncoder,
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            // Autenticar con AuthenticationManager usando email
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Obtener UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // IMPORTANTE: Buscar el username asociado al email para guardarlo en el token
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Generar token con username (no email) y autoridades
            String token = jwtUtil.generateToken(usuario.getUsername(), userDetails.getAuthorities());
            
            return new AuthResponse(token);
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error durante autenticación: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Credenciales inválidas", e);
        }
    }

    public void register(RegisterRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El usuario ya existe con ese email");
        }
        
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        
        // Normalizar el rol para evitar problemas de case-sensitivity
        String rol = request.getRol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("CLIENTE")) {
            rol = "CLIENTE";  // Valor por defecto si el rol no es válido
        }
        usuario.setRol(rol);

        usuarioRepository.save(usuario);
    }
}