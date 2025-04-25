package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
    List<Factura> findByClienteId(String clienteId);

    List<Factura> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("{'cliente.usuario.username': ?0}")
    List<Factura> findByClienteUsuarioUsername(String username);

    @Query("{'cliente.usuario.email': ?0}")
    List<Factura> findByClienteUsuarioEmail(String email);

    // También agregamos este método como respaldo
    @Query("{$or: [{'cliente.usuario.username': ?0}, {'cliente.usuario.email': ?0}]}")
    List<Factura> findByClienteUsuarioUsernameOrEmail(String usernameOrEmail);
}
