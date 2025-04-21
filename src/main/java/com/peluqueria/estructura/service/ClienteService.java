package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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

    public Cliente findByUsuarioUsername(String username) {
        return clienteRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con el username: " + username));
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(String id) {
        clienteRepository.deleteById(id);
    }
}
