package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
    // Consulta básica por clienteId directamente (esto funciona con DBRef)
    List<Factura> findByClienteId(String clienteId);

    List<Factura> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // NUEVA CONSULTA: Busca directamente por la referencia a cliente
    @Query("{'cliente.$id': ?0}")
    List<Factura> findByClienteRef(String clienteId);

    // NUEVA CONSULTA: Busca facturas por ID, para diagnóstico
    @Query(value = "{}", fields = "{'id': 1, 'cliente': 1, 'estado': 1}")
    List<Factura> findAllFacturasResumen();

    // Consultamos directamente el ObjectId de la referencia
    @Query("{'cliente.$id': ?0}")
    List<Factura> findByClienteIdRef(String clienteId);

    // Mantén estas consultas pero ajusta el código para usarlas solo cuando
    // corresponda
    @Query("{'cliente.usuario.username': ?0}")
    List<Factura> findByClienteUsuarioUsername(String username);

    @Query("{'cliente.usuario.email': ?0}")
    List<Factura> findByClienteUsuarioEmail(String email);

    @Query("{$or: [{'cliente.usuario.username': ?0}, {'cliente.usuario.email': ?0}, {'cliente.id': ?0}]}")
    List<Factura> findByClienteIdOrUsername(String idOrUsername);

    // Mantén esta consulta como respaldo para casos complejos
    @Query(value = "{$where: 'function() { " +
            "return (this.cliente && (this.cliente.id == ?0 || " +
            "(this.cliente.usuario && " +
            "(this.cliente.usuario.username == ?1 || this.cliente.usuario.email == ?1)))); }'}")
    List<Factura> findAllClientFacturas(String clienteId, String usernameOrEmail);
}
