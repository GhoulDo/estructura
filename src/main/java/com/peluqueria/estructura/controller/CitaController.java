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
    public ResponseEntity<Map<String, Object>> getAllCitas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {
        
        return ResponseEntity.ok(citaService.getAllCitasOrganized(
                estado, mascotaId, servicioId, fechaDesde, fechaHasta, ordenarPor, direccion));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getCitaById(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.getCitaById(id));
    }
    
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Cita>> getCitasByMascotaId(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(citaService.getCitasByMascotaId(mascotaId));
    }
    
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> getCitasByFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.getCitasByFecha(fecha));
    }
    
    @GetMapping("/hoy")
    public ResponseEntity<List<Cita>> getCitasHoy() {
        return ResponseEntity.ok(citaService.getCitasByFecha(LocalDate.now()));
    }

    @PostMapping
    public ResponseEntity<Cita> createCita(@RequestBody Cita cita) {
        return ResponseEntity.ok(citaService.createCita(cita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> updateCita(@PathVariable Long id, @RequestBody Cita cita) {
        return ResponseEntity.ok(citaService.updateCita(id, cita));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable Long id) {
        citaService.deleteCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar")
    public ResponseEntity<String> validarCita(
        @RequestParam Long mascotaId, 
        @RequestParam Long servicioId, 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, 
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        String resultado = citaService.verificarDisponibilidadCita(mascotaId, servicioId, fecha, hora);
        return ResponseEntity.ok(resultado);
    }
}