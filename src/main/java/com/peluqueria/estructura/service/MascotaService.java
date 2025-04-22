package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.MascotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private static final Logger logger = LoggerFactory.getLogger(MascotaService.class);
    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public MascotaService(MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
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
            // Encontramos el cliente asociado a ese usuario
            Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioUsername(username);

            if (!clienteOpt.isPresent()) {
                logger.warn("No se encontró cliente para el usuario: {}", username);
                return Collections.emptyList();
            }

            Cliente cliente = clienteOpt.get();
            logger.debug("Cliente encontrado para el usuario: {}, ID: {}", username, cliente.getId());

            // Buscar mascotas por el ID del cliente
            List<Mascota> mascotas = mascotaRepository.findByClienteId(cliente.getId());
            logger.debug("Encontradas {} mascotas para el cliente: {}", mascotas.size(), cliente.getId());

            return mascotas;
        } catch (Exception e) {
            logger.error("Error al buscar mascotas para el usuario: {}", username, e);
            throw e;
        }
    }

    /**
     * Busca una mascota por su ID y el username del usuario
     */
    public Optional<Mascota> findByIdAndClienteUsuarioUsername(String id, String username) {
        // Encontramos el cliente asociado a ese usuario
        Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioUsername(username);

        if (!clienteOpt.isPresent()) {
            return Optional.empty();
        }

        Cliente cliente = clienteOpt.get();

        // Intentamos encontrar la mascota por su id y el id del cliente
        return mascotaRepository.findByIdAndClienteId(id, cliente.getId());
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
