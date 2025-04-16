package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.service.ServicioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<List<Servicio>> getAllServicios() {
        return ResponseEntity.ok(servicioService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Servicio> getServicioById(@PathVariable String id) {
        return servicioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Servicio> createServicio(@RequestBody Servicio servicio) {
        Servicio nuevoServicio = servicioService.save(servicio);
        return new ResponseEntity<>(nuevoServicio, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Servicio> updateServicio(@PathVariable String id, @RequestBody Servicio servicio) {
        return servicioService.findById(id)
                .map(servicioExistente -> {
                    servicio.setId(id);
                    return ResponseEntity.ok(servicioService.save(servicio));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Servicio> patchServicio(@PathVariable String id, @RequestBody Servicio servicioActualizado) {
        return servicioService.findById(id)
                .map(servicioExistente -> {
                    // Actualiza solo los campos no nulos
                    if (servicioActualizado.getNombre() != null) {
                        servicioExistente.setNombre(servicioActualizado.getNombre());
                    }
                    if (servicioActualizado.getDuracion() > 0) {
                        servicioExistente.setDuracion(servicioActualizado.getDuracion());
                    }
                    if (servicioActualizado.getPrecio() != null) {
                        servicioExistente.setPrecio(servicioActualizado.getPrecio());
                    }
                    
                    return ResponseEntity.ok(servicioService.save(servicioExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServicio(@PathVariable String id) {
        return servicioService.findById(id)
                .map(servicio -> {
                    servicioService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}