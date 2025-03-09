package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByClienteId(Long clienteId);
    List<Mascota> findByClienteUsuarioId(Long usuarioId);
}

