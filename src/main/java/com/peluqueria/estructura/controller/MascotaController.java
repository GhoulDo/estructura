package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.service.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas(Authentication authentication) {
        return ResponseEntity.ok(mascotaService.getMascotasByUsuario(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(mascotaService.getMascotaByIdAndUsuario(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Mascota> createMascota(@RequestBody Mascota mascota, Authentication authentication) {
        return ResponseEntity.ok(mascotaService.createMascota(mascota, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> updateMascota(@PathVariable Long id, @RequestBody Mascota mascota, Authentication authentication) {
        return ResponseEntity.ok(mascotaService.updateMascota(id, mascota, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id, Authentication authentication) {
        mascotaService.deleteMascota(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

