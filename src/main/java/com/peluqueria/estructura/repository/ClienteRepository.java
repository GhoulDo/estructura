package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Optional<Cliente> findByUsuarioId(String usuarioId);

    @Query("{ 'usuario.$id': ?0 }")
    Optional<Cliente> findByUsuarioIdRef(String usuarioId);
}
