package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Optional<Cliente> findByUsuarioId(String usuarioId);

    // Consulta para buscar clientes por el username del usuario asociado
    @Query("{'usuario.username': {$regex: '^?0$', $options: 'i'}}")
    List<Cliente> findByUsuarioUsername(String username);

    // Consulta para buscar por email del cliente
    List<Cliente> findByEmail(String email);
}
