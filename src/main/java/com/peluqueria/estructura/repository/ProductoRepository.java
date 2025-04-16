package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {
    // Métodos personalizados si son necesarios
}

