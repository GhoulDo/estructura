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

@Service
public class AuthenticationService {

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
            // Buscar usuario directamente en el repositorio
            Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el email proporcionado."));

            // Validar contraseña
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                throw new IllegalArgumentException("La contraseña es incorrecta.");
            }

            // Generar token JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
            return new AuthResponse(token);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Credenciales inválidas: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el login: " + e.getMessage());
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

        // Guardar el usuario primero para obtener el ID generado
        usuario = usuarioRepository.save(usuario);
        
        // Si el usuario es un CLIENTE, crear una entrada en la tabla clientes
        if (rol.equals("CLIENTE")) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            cliente.setNombre(request.getUsername()); // Usar username como nombre inicial
            cliente.setEmail(request.getEmail());     // Usar el mismo email
            cliente.setTelefono(""); // Inicializar campo vacío
            cliente.setDireccion(""); // Inicializar campo vacío
            cliente.setMascotas(null); // Inicializar mascotas como null
            
            clienteRepository.save(cliente);
        }
    }
}