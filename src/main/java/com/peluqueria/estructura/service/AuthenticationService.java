package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import com.peluqueria.estructura.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ClienteRepository clienteRepository;

    public AuthenticationService(
            UsuarioRepository usuarioRepository,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            ClienteRepository clienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.clienteRepository = clienteRepository;
    }

    public AuthResponse login(AuthRequest loginRequest) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        logger.warn("Usuario no encontrado con el email: {}", loginRequest.getEmail());
                        return new IllegalArgumentException("Usuario no encontrado con el email proporcionado.");
                    });

            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                logger.warn("Contraseña incorrecta para el usuario: {}", loginRequest.getEmail());
                throw new IllegalArgumentException("La contraseña es incorrecta.");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
            return new AuthResponse(token);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de autenticación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error interno al procesar el login: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el login: " + e.getMessage());
        }
    }

    public void register(RegisterRequest request) {
        try {
            if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("Intento de registro con email ya existente: {}", request.getEmail());
                throw new RuntimeException("El usuario ya existe con ese email");
            }

            if (usuarioRepository.existsByUsername(request.getUsername())) {
                logger.warn("Intento de registro con nombre de usuario ya existente: {}", request.getUsername());
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }

            Usuario usuario = new Usuario();
            usuario.setUsername(request.getUsername());
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
            usuario.setEmail(request.getEmail());

            String rol = request.getRol().toUpperCase();
            if (!rol.equals("ADMIN") && !rol.equals("CLIENTE")) {
                rol = "CLIENTE";
            }
            usuario.setRol(rol);

            usuario = usuarioRepository.save(usuario);

            if (rol.equals("CLIENTE")) {
                Cliente cliente = new Cliente();
                cliente.setUsuario(usuario);
                cliente.setNombre(request.getUsername());
                cliente.setEmail(request.getEmail());
                cliente.setTelefono("");
                cliente.setDireccion("");
                cliente.setMascotas(null);

                clienteRepository.save(cliente);
            }
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void invalidateToken(String username) {
        jwtUtil.invalidateToken(username);
    }
}