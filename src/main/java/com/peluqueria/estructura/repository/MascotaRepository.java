package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Mascota;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends MongoRepository<Mascota, String> {
    List<Mascota> findByClienteId(String clienteId);

    // Consultas personalizadas que no usan path reference anidada problemática
    @Query("{'cliente.$id': { $in: ?0 }}")
    List<Mascota> findByClienteIds(List<String> clienteIds);

    @Query("{ '_id': ?0, 'cliente.$id': ?1 }")
    Optional<Mascota> findByIdAndClienteId(String id, String clienteId);
}
