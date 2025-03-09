package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.CitaService;
import com.peluqueria.estructura.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;

    public CitaController(CitaService citaService, UsuarioService usuarioService) {
        this.citaService = citaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Cita>> getAllCitas(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getRol().equals("ADMIN")) {
            return ResponseEntity.ok(citaService.getAllCitas());
        } else {
            return ResponseEntity.ok(citaService.getCitasByUsuarioId(usuario.getId()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCitaById(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cita cita = citaService.getCitaById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        if (usuario.getRol().equals("ADMIN") || cita.getMascota().getCliente().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.ok(cita);
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para ver esta cita.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createCita(@RequestBody Cita cita, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getRol().equals("ADMIN") || cita.getMascota().getCliente().getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.ok(citaService.createCita(cita));
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para crear esta cita.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCita(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cita cita = citaService.getCitaById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        if (usuario.getRol().equals("ADMIN") || cita.getMascota().getCliente().getUsuario().getId().equals(usuario.getId())) {
            citaService.deleteCita(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para eliminar esta cita.");
        }
    }
}

