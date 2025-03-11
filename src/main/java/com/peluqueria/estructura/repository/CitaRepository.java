package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByMascotaId(Long mascotaId);
    List<Cita> findByFecha(LocalDate fecha);

    @Query(value = "SELECT validar_cita(:mascotaId, :servicioId, :fecha, :hora)", nativeQuery = true)
    String validarCita(
        @Param("mascotaId") Long mascotaId, 
        @Param("servicioId") Long servicioId, 
        @Param("fecha") LocalDate fecha, 
        @Param("hora") LocalTime hora
    );
}

