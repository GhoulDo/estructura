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
     * Encuentra un cliente por el username de su usuario asociado
     */
    public Cliente findByUsuarioUsername(String username) {
        logger.debug("Buscando cliente para usuario: {}", username);

        // Buscar usuario por username
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseGet(() -> {
                    logger.debug("Usuario no encontrado con username exacto, probando case-insensitive");
                    return usuarioRepository.findByUsernameIgnoreCase(username)
                            .orElseThrow(() -> {
                                logger.error("Usuario no encontrado con username: {}", username);
                                return new ResourceNotFoundException("Usuario", "username", username);
                            });
                });

        logger.debug("Usuario encontrado: {}", usuario.getUsername());

        // Buscar cliente por usuario ID
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> {
                    logger.error("Cliente no encontrado para usuario ID: {}", usuario.getId());
                    return new ResourceNotFoundException("Cliente", "usuarioId", usuario.getId());
                });

        logger.debug("Cliente encontrado: {}", cliente.getNombre());
        return cliente;
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(String id) {
        clienteRepository.deleteById(id);
    }
}
