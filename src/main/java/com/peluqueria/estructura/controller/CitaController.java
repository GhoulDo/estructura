package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.service.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public ResponseEntity<List<Cita>> getAllCitas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String mascotaId,
            @RequestParam(required = false) String servicioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {
        
        return ResponseEntity.ok(citaService.findAll());
    }
    
    @GetMapping("/organizadas")
    public ResponseEntity<Map<LocalDate, List<Cita>>> getCitasOrganizadas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String mascotaId,
            @RequestParam(required = false) String servicioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {
        
        // Este método necesitará ser implementado en el nuevo CitaService
        return ResponseEntity.ok(citaService.getCitasOrganizadasPorFecha());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getCitaById(@PathVariable String id) {
        return citaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Cita>> getCitasByMascotaId(@PathVariable String mascotaId) {
        return ResponseEntity.ok(citaService.findByMascotaId(mascotaId));
    }
    
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> getCitasByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.findByFecha(fecha));
    }
    
    @GetMapping("/hoy")
    public ResponseEntity<List<Cita>> getCitasHoy() {
        return ResponseEntity.ok(citaService.findByFecha(LocalDate.now()));
    }

    @PostMapping
    public ResponseEntity<Cita> createCita(@RequestBody Cita cita) {
        return ResponseEntity.ok(citaService.save(cita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> updateCita(@PathVariable String id, @RequestBody Cita cita) {
        return citaService.findById(id)
            .map(existingCita -> {
                cita.setId(id);
                return ResponseEntity.ok(citaService.save(cita));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable String id) {
        citaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar")
    public ResponseEntity<String> validarCita(
        @RequestParam String mascotaId, 
        @RequestParam String servicioId, 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        // Este método necesitará ser implementado en el servicio
        return ResponseEntity.ok("Cita disponible");
    }
}