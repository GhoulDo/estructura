package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.service.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas(Authentication authentication) {
        return ResponseEntity.ok(mascotaService.findByClienteUsuarioUsername(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable String id, Authentication authentication) {
        Optional<Mascota> mascota = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        return mascota.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mascota> createMascota(@RequestBody Mascota mascota, Authentication authentication) {
        // La lógica de asociar la mascota al cliente correcto debe estar implementada en el servicio
        return ResponseEntity.ok(mascotaService.save(mascota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> updateMascota(@PathVariable String id, @RequestBody Mascota mascota, Authentication authentication) {
        Optional<Mascota> existingMascota = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        return existingMascota.map(m -> {
            mascota.setId(id);
            return ResponseEntity.ok(mascotaService.save(mascota));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable String id, Authentication authentication) {
        Optional<Mascota> mascota = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        if (mascota.isPresent()) {
            mascotaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

