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

    // Consulta más robusta que busca por cualquier parte del objeto cliente
    @Query("{$or: [{'cliente.usuario.username': ?0}, {'cliente.usuario.email': ?0}, {'cliente.id': ?0}]}")
    List<Factura> findByClienteIdOrUsername(String idOrUsername);

    // Consulta por ID del cliente si las anteriores fallan
    @Query("{'cliente.id': ?0}")
    List<Factura> findByClienteIdDirectly(String clienteId);

    // Buscar todas las facturas creadas por un cliente (identificado por su ID)
    // independientemente de la estructura
    @Query(value = "{$where: 'function() { " +
            "return (this.cliente && (this.cliente.id == ?0 || " +
            "(this.cliente.usuario && " +
            "(this.cliente.usuario.username == ?1 || this.cliente.usuario.email == ?1)))); }'}")
    List<Factura> findAllClientFacturas(String clienteId, String usernameOrEmail);
}
