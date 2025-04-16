package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends MongoRepository<Servicio, String> {
    // Podemos agregar consultas personalizadas si son necesarias
}


