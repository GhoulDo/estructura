package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.repository.CitaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public Map<String, Object> getAllCitasOrganized(
            String estado, 
            Long mascotaId, 
            Long servicioId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String ordenarPor,
            String direccion) {
        
        // Obtener todas las citas
        List<Cita> citas = citaRepository.findAll();
        
        // Aplicar filtros si existen
        if (estado != null && !estado.isEmpty()) {
            citas = citas.stream()
                    .filter(cita -> estado.equalsIgnoreCase(cita.getEstado()))
                    .collect(Collectors.toList());
        }
        
        if (mascotaId != null) {
            citas = citas.stream()
                    .filter(cita -> cita.getMascota() != null && mascotaId.equals(cita.getMascota().getId()))
                    .collect(Collectors.toList());
        }
        
        if (servicioId != null) {
            citas = citas.stream()
                    .filter(cita -> cita.getServicio() != null && servicioId.equals(cita.getServicio().getId()))
                    .collect(Collectors.toList());
        }
        
        if (fechaDesde != null) {
            citas = citas.stream()
                    .filter(cita -> cita.getFecha() != null && !cita.getFecha().isBefore(fechaDesde))
                    .collect(Collectors.toList());
        }
        
        if (fechaHasta != null) {
            citas = citas.stream()
                    .filter(cita -> cita.getFecha() != null && !cita.getFecha().isAfter(fechaHasta))
                    .collect(Collectors.toList());
        }
        
        // Ordenar las citas
        sortCitas(citas, ordenarPor, direccion);
        
        // Organizar por fecha
        Map<LocalDate, List<Cita>> citasPorFecha = new TreeMap<>();
        for (Cita cita : citas) {
            if (cita.getFecha() != null) {
                citasPorFecha.computeIfAbsent(cita.getFecha(), k -> new ArrayList<>()).add(cita);
            }
        }
        
        // Contar citas por estado
        Map<String, Long> conteoEstados = citas.stream()
                .filter(cita -> cita.getEstado() != null)
                .collect(Collectors.groupingBy(Cita::getEstado, Collectors.counting()));
        
        // Construir respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("total", citas.size());
        response.put("citas", citas);
        response.put("citasPorFecha", citasPorFecha);
        response.put("citasPorEstado", conteoEstados);
        
        return response;
    }

    private void sortCitas(List<Cita> citas, String ordenarPor, String direccion) {
        Comparator<Cita> comparator = null;
        
        switch (ordenarPor.toLowerCase()) {
            case "fecha":
                comparator = Comparator.comparing(Cita::getFecha, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "hora":
                comparator = Comparator.comparing(Cita::getHora, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "estado":
                comparator = Comparator.comparing(Cita::getEstado, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                comparator = Comparator.comparing(Cita::getFecha, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }
        
        if ("desc".equalsIgnoreCase(direccion)) {
            comparator = comparator.reversed();
        }
        
        citas.sort(comparator);
    }

    public Cita getCitaById(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }
    
    public List<Cita> getCitasByMascotaId(Long mascotaId) {
        return citaRepository.findByMascotaId(mascotaId);
    }
    
    public List<Cita> getCitasByFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
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