package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cita;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends MongoRepository<Cita, String> {
    List<Cita> findByMascotaId(String mascotaId);
    List<Cita> findByMascotaClienteId(String clienteId);
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findByMascotaClienteUsuarioUsername(String username);
}

