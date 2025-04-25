package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        logger.info("ClienteService inicializado correctamente");
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findById(String id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> findByUsuarioId(String usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Encuentra un cliente por el username o email de su usuario asociado
     */
    public Cliente findByUsuarioUsername(String usernameOrEmail) {
        logger.debug("Buscando cliente para usuario: {}", usernameOrEmail);

        // Buscar usuario por username o por email
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> {
                    logger.debug("Usuario no encontrado con username exacto, probando email");
                    return usuarioRepository.findByEmail(usernameOrEmail)
                            .orElseGet(() -> {
                                logger.debug("Usuario no encontrado con email, probando username case-insensitive");
                                return usuarioRepository.findByUsernameIgnoreCase(usernameOrEmail)
                                        .orElseThrow(() -> {
                                            logger.error("Usuario no encontrado con username o email: {}",
                                                    usernameOrEmail);
                                            return new ResourceNotFoundException("Usuario", "username/email",
                                                    usernameOrEmail);
                                        });
                            });
                });

        logger.debug("Usuario encontrado: {} (ID: {})", usuario.getUsername(), usuario.getId());

        // Buscar cliente por usuario ID
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> {
                    logger.error("Cliente no encontrado para usuario ID: {}", usuario.getId());
                    return new ResourceNotFoundException("Cliente", "usuarioId", usuario.getId());
                });

        logger.debug("Cliente encontrado: {} (ID: {})", cliente.getNombre(), cliente.getId());
        return cliente;
    }

    /**
     * Versión opcional del método que no lanza excepciones
     */
    public Optional<Cliente> findOptionalByUsuarioUsername(String usernameOrEmail) {
        logger.debug("Buscando cliente (optional) para usuario: {}", usernameOrEmail);

        try {
            // Primero intentamos buscar por username
            Optional<Usuario> usuario = usuarioRepository.findByUsername(usernameOrEmail);

            // Si no lo encontramos, intentamos por email
            if (usuario.isEmpty()) {
                usuario = usuarioRepository.findByEmail(usernameOrEmail);
            }

            // Si aún no lo encontramos, intentamos case-insensitive
            if (usuario.isEmpty()) {
                usuario = usuarioRepository.findByUsernameIgnoreCase(usernameOrEmail);
            }

            // Si no encontramos el usuario, devolvemos empty
            if (usuario.isEmpty()) {
                logger.warn("No se encontró usuario con username o email: {}", usernameOrEmail);
                return Optional.empty();
            }

            // Buscamos el cliente asociado
            Optional<Cliente> cliente = clienteRepository.findByUsuarioId(usuario.get().getId());

            if (cliente.isEmpty()) {
                logger.warn("No se encontró cliente para usuario ID: {}", usuario.get().getId());
            }

            return cliente;
        } catch (Exception e) {
            logger.error("Error al buscar cliente por username/email {}: {}", usernameOrEmail, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(String id) {
        clienteRepository.deleteById(id);
    }
}
