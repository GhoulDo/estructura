package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.AuthRequest;
import com.peluqueria.estructura.dto.AuthResponse;
import com.peluqueria.estructura.dto.RegisterRequest;
import com.peluqueria.estructura.dto.UserProfileDTO;
import com.peluqueria.estructura.dto.UpdateProfileRequest;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
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

import java.util.regex.Pattern;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

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
            // Normalizar el email
            String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();

            // Log para verificar el email normalizado
            logger.debug("Intentando autenticar usuario con email: {}", normalizedEmail);

            // Validar el formato del email
            if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
                logger.warn("Formato de email inválido: {}", normalizedEmail);
                throw new IllegalArgumentException("El formato del email es inválido.");
            }

            Usuario usuario = usuarioRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> {
                        logger.warn("Usuario no encontrado con el email: {}", normalizedEmail);
                        return new IllegalArgumentException("Usuario no encontrado con el email proporcionado.");
                    });

            // Log para verificar que el usuario fue encontrado
            logger.debug("Usuario encontrado: {}", usuario.getUsername());

            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                logger.warn("Contraseña incorrecta para el usuario: {}", normalizedEmail);
                throw new IllegalArgumentException("La contraseña es incorrecta.");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

            // Log para verificar que el token fue generado
            logger.debug("Token generado exitosamente para el usuario: {}", usuario.getUsername());

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

    /**
     * Obtiene los datos del perfil del usuario actualmente autenticado
     */
    public UserProfileDTO getCurrentUserProfile(String username) {
        logger.debug("Obteniendo perfil para el usuario: {}", username);

        try {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseGet(() -> usuarioRepository.findByEmail(username)
                            .orElseThrow(() -> {
                                logger.error("Usuario no encontrado con username/email: {}", username);
                                return new ResourceNotFoundException("Usuario", "username/email", username);
                            }));

            logger.debug("Usuario encontrado: ID={}, Rol={}", usuario.getId(), usuario.getRol());

            // Buscar el cliente asociado al usuario si existe
            Cliente cliente = null;
            try {
                cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (cliente != null) {
                    logger.debug("Cliente encontrado para el usuario: {} (ID: {})", cliente.getNombre(),
                            cliente.getId());
                }
            } catch (Exception e) {
                logger.warn("Error al buscar cliente para el usuario {}: {}", username, e.getMessage());
                // No hacemos rethrow porque el perfil puede existir sin cliente
            }

            return UserProfileDTO.fromEntities(usuario, cliente);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener perfil del usuario {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error al obtener perfil de usuario: " + e.getMessage());
        }
    }

    /**
     * Actualiza el perfil del usuario autenticado
     */
    public UserProfileDTO updateProfile(String username, UpdateProfileRequest request) {
        logger.debug("Actualizando perfil para usuario: {}", username);

        try {
            // Buscar el usuario por username o email
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseGet(() -> usuarioRepository.findByEmail(username)
                            .orElseThrow(() -> {
                                logger.error("Usuario no encontrado con username/email: {}", username);
                                return new ResourceNotFoundException("Usuario", "username/email", username);
                            }));

            logger.debug("Usuario encontrado: ID={}", usuario.getId());

            // Verificar la contraseña actual si se provee una nueva contraseña
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                if (request.getCurrentPassword() == null
                        || !passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
                    logger.warn("Contraseña actual incorrecta al intentar cambiar contraseña para usuario: {}",
                            username);
                    throw new IllegalArgumentException("La contraseña actual es incorrecta");
                }

                usuario.setPassword(passwordEncoder.encode(request.getPassword()));
                logger.debug("Contraseña actualizada para el usuario: {}", username);
            }

            // Actualizar email si cambió y no está en uso por otro usuario
            if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
                if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                    logger.warn("Intento de actualizar a un email ya existente: {}", request.getEmail());
                    throw new IllegalArgumentException("El email ya está en uso por otro usuario");
                }
                usuario.setEmail(request.getEmail());
            }

            // Actualizar username si cambió y no está en uso por otro usuario
            if (request.getUsername() != null && !request.getUsername().equals(usuario.getUsername())) {
                if (usuarioRepository.existsByUsername(request.getUsername())) {
                    logger.warn("Intento de actualizar a un username ya existente: {}", request.getUsername());
                    throw new IllegalArgumentException("El nombre de usuario ya está en uso");
                }
                usuario.setUsername(request.getUsername());
            }

            // Guardar los cambios del usuario
            usuario = usuarioRepository.save(usuario);

            // Buscar y actualizar la información del cliente si existe
            Cliente cliente = null;
            try {
                cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (cliente != null) {
                    logger.debug("Cliente encontrado para actualizar: ID={}", cliente.getId());

                    // Actualizar campos del cliente
                    if (request.getNombre() != null) {
                        cliente.setNombre(request.getNombre());
                    }
                    if (request.getTelefono() != null) {
                        cliente.setTelefono(request.getTelefono());
                    }
                    if (request.getDireccion() != null) {
                        cliente.setDireccion(request.getDireccion());
                    }

                    // Asegurarse de que el email del cliente esté sincronizado con el del usuario
                    cliente.setEmail(usuario.getEmail());

                    cliente = clienteRepository.save(cliente);
                    logger.debug("Cliente actualizado exitosamente");
                }
            } catch (Exception e) {
                logger.warn("Error al intentar actualizar cliente para el usuario {}: {}", username, e.getMessage());
                // No hacemos rethrow porque el perfil puede actualizarse sin actualizar el
                // cliente
            }

            logger.info("Perfil actualizado exitosamente para el usuario: {}", username);
            return UserProfileDTO.fromEntities(usuario, cliente);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar perfil del usuario {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar perfil: " + e.getMessage());
        }
    }
}