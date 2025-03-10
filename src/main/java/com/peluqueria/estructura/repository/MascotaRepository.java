package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByClienteId(Long clienteId);
    List<Mascota> findByClienteUsuarioUsername(String username);
    Optional<Mascota> findByIdAndClienteUsuarioUsername(Long id, String username);
}

