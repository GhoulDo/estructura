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
    
    // Agregamos este método para buscar facturas por username del usuario
    @Query("{'cliente.usuario.username': ?0}")
    List<Factura> findByClienteUsuarioUsername(String username);
}

