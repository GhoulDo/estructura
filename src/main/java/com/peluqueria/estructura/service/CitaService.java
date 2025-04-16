package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    @Autowired
    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(String id) {
        return citaRepository.findById(id);
    }

    public List<Cita> findByMascotaId(String mascotaId) {
        return citaRepository.findByMascotaId(mascotaId);
    }

    public List<Cita> findByMascotaClienteId(String clienteId) {
        return citaRepository.findByMascotaClienteId(clienteId);
    }

    public List<Cita> findByFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    public List<Cita> findByMascotaClienteUsuarioUsername(String username) {
        return citaRepository.findByMascotaClienteUsuarioUsername(username);
    }

    public Cita save(Cita cita) {
        return citaRepository.save(cita);
    }

    public void deleteById(String id) {
        citaRepository.deleteById(id);
    }
    
    /**
     * Organiza las citas por fecha para facilitar la visualización en un calendario
     * @return Un mapa donde la clave es la fecha y el valor es una lista de citas para esa fecha
     */
    public Map<LocalDate, List<Cita>> getCitasOrganizadasPorFecha() {
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        // Agrupamos las citas por fecha
        return todasLasCitas.stream()
                .collect(Collectors.groupingBy(Cita::getFecha));
    }
}