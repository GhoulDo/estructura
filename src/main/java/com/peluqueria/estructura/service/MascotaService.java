package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.repository.MascotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;

@Service
public class MascotaService {

    private static final Logger logger = LoggerFactory.getLogger(MascotaService.class);
    private final MascotaRepository mascotaRepository;
    private final ClienteService clienteService;

    @Autowired
    public MascotaService(MascotaRepository mascotaRepository, ClienteService clienteService) {
        this.mascotaRepository = mascotaRepository;
        this.clienteService = clienteService;
    }

    public List<Mascota> findAll() {
        return mascotaRepository.findAll();
    }

    public Optional<Mascota> findById(String id) {
        return mascotaRepository.findById(id);
    }

    public List<Mascota> findByClienteId(String clienteId) {
        return mascotaRepository.findByClienteId(clienteId);
    }

    /**
     * Busca las mascotas de un usuario a través de su username.
     * Primero busca el cliente asociado al usuario y luego las mascotas de ese
     * cliente.
     */
    public List<Mascota> findByClienteUsuarioUsername(String username) {
        logger.debug("Buscando mascotas para el usuario: {}", username);
        try {
            // Usar el ClienteService para obtener el cliente
            Cliente cliente = clienteService.findByUsuarioUsername(username);
            logger.debug("Cliente encontrado para el usuario: {}, ID: {}", username, cliente.getId());

            // Buscar mascotas por el ID del cliente
            try {
                List<Mascota> mascotas = mascotaRepository.findByClienteId(cliente.getId());
                logger.debug("Encontradas {} mascotas para el cliente: {}", mascotas.size(), cliente.getId());
                return mascotas;
            } catch (DataAccessException e) {
                logger.error("Error de acceso a datos al buscar mascotas para el cliente ID {}: {}",
                        cliente.getId(), e.getMessage(), e);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Error al buscar mascotas para el usuario: {}: {}", username, e.getMessage(), e);
            return Collections.emptyList(); // Devuelve lista vacía en lugar de lanzar excepción
        }
    }

    /**
     * Busca una mascota por su ID y el username del usuario
     */
    public Optional<Mascota> findByIdAndClienteUsuarioUsername(String id, String username) {
        try {
            // Usar el ClienteService para obtener el cliente
            Cliente cliente = clienteService.findByUsuarioUsername(username);

            // Intentamos encontrar la mascota por su id y el id del cliente
            try {
                return mascotaRepository.findByIdAndClienteId(id, cliente.getId());
            } catch (DataAccessException e) {
                logger.error("Error de acceso a datos al buscar mascota ID {} para el cliente ID {}: {}",
                        id, cliente.getId(), e.getMessage(), e);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error al buscar mascota ID {} para el usuario: {}: {}",
                    id, username, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Mascota save(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public void deleteById(String id) {
        mascotaRepository.deleteById(id);
    }

    public void saveFoto(String id, byte[] foto) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        mascota.setFoto(foto);
        mascotaRepository.save(mascota);
    }

    public byte[] getFoto(String id) {
        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        return (mascota != null) ? mascota.getFoto() : null;
    }
}
