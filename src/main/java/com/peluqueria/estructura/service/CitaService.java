package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> getAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> getCitaById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita createCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public void deleteCita(Long id) {
        citaRepository.deleteById(id);
    }

    public List<Cita> getCitasByUsuarioId(Long usuarioId) {
        return citaRepository.findByMascotaClienteUsuarioId(usuarioId);
    }

    public String registrarCita(Long mascotaId, Long servicioId, LocalDate fecha, LocalTime hora, String estado) {
        Cita cita = new Cita();
        cita.setMascota(new Mascota());
        cita.getMascota().setId(mascotaId);
        cita.setServicio(new Servicio());
        cita.getServicio().setId(servicioId);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(estado);
        citaRepository.save(cita);
        return "Cita registrada correctamente";
    }
}


