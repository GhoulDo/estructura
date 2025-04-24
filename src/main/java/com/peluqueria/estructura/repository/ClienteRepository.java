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

    @Query("{'email': ?0}")
    Optional<Cliente> findByEmail(String email);

    @Query("{'usuario.username': ?0}")
    List<Cliente> findByUsuarioUsername(String username);
}
