package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Mascota;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends MongoRepository<Mascota, String> {
    List<Mascota> findByClienteId(String clienteId);
    List<Mascota> findByClienteUsuarioUsername(String username);
    Optional<Mascota> findByIdAndClienteUsuarioUsername(String id, String username);
}

