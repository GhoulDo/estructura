package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
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
     * Encuentra un cliente por el nombre de usuario.
     * Primero busca el usuario por username, luego busca el cliente por el ID del
     * usuario.
     */
    public Cliente findByUsuarioUsername(String username) {
        logger.debug("Buscando cliente por username: {}", username);
        try {
            // Primero, obtener el usuario por su username
            Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

            if (!usuario.isPresent()) {
                logger.warn("Usuario no encontrado con username: {}", username);
                throw new RuntimeException("Usuario no encontrado con el username: " + username);
            }

            // Luego, buscar el cliente por el ID del usuario
            Optional<Cliente> cliente = clienteRepository.findByUsuarioId(usuario.get().getId());

            if (!cliente.isPresent()) {
                logger.warn("Cliente no encontrado para el usuario con ID: {}", usuario.get().getId());
                throw new RuntimeException("Cliente no encontrado para el usuario: " + username);
            }

            return cliente.get();
        } catch (Exception e) {
            logger.error("Error al buscar cliente por username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error al buscar cliente: " + e.getMessage());
        }
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(String id) {
        clienteRepository.deleteById(id);
    }
}
