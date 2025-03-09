package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByMascotaId(Long mascotaId);
    List<Cita> findByMascotaClienteUsuarioId(Long usuarioId);
}

