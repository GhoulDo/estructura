package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import com.peluqueria.estructura.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(request.getEmail());
        if (usuario.isPresent() && passwordEncoder.matches(request.getPassword(), usuario.get().getPassword())) {
            String token = jwtUtil.generateToken(usuario.get().getEmail());
            return new AuthResponse(token);
        }
        throw new RuntimeException("Credenciales inválidas");
    }

    public void register(RegisterRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El usuario con el correo electrónico " + request.getEmail() + " ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());

        usuarioRepository.save(usuario);

        // Crear cliente asociado al usuario si el rol es CLIENTE
        if ("CLIENTE".equals(request.getRol())) {
            Cliente cliente = new Cliente();
            cliente.setNombre(request.getUsername());
            cliente.setEmail(request.getEmail());
            cliente.setUsuario(usuario);
            // Los otros campos quedarán en null por defecto
            clienteRepository.save(cliente);
        }
    }
}
