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
}


