package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Rol;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RolRepository extends MongoRepository<Rol, String> {
    Optional<Rol> findByNombre(String nombre);
}

