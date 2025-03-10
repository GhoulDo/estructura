package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.repository.CitaRepository;
import org.springframework.stereotype.Service;

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

    public Cita getCitaById(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    public Cita createCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public Cita updateCita(Long id, Cita cita) {
        Cita existingCita = getCitaById(id);
        existingCita.setFecha(cita.getFecha());
        existingCita.setHora(cita.getHora());
        existingCita.setEstado(cita.getEstado());
        existingCita.setMascota(cita.getMascota());
        existingCita.setServicio(cita.getServicio());
        return citaRepository.save(existingCita);
    }

    public void deleteCita(Long id) {
        citaRepository.deleteById(id);
    }
}


