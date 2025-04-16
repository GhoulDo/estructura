package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
    List<Factura> findByClienteId(String clienteId);
    List<Factura> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Factura> findByClienteUsuarioUsername(String username);
}

