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
        return ResponseEntity.ok(servicioService.getAllServicios());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Servicio> getServicioById(@PathVariable Long id) {
        return servicioService.obtenerServicioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Servicio> createServicio(@RequestBody Servicio servicio) {
        Servicio nuevoServicio = servicioService.createServicio(servicio);
        return new ResponseEntity<>(nuevoServicio, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Servicio> updateServicio(@PathVariable Long id, @RequestBody Servicio servicio) {
        return servicioService.obtenerServicioPorId(id)
                .map(servicioExistente -> {
                    servicio.setId(id);
                    return ResponseEntity.ok(servicioService.guardarServicio(servicio));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Servicio> patchServicio(@PathVariable Long id, @RequestBody Servicio servicioActualizado) {
        return servicioService.obtenerServicioPorId(id)
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
                    
                    return ResponseEntity.ok(servicioService.guardarServicio(servicioExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServicio(@PathVariable Long id) {
        return servicioService.obtenerServicioPorId(id)
                .map(servicio -> {
                    servicioService.eliminarServicio(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}