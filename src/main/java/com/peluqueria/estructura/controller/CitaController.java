package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.CitaService;
import com.peluqueria.estructura.service.MascotaService;
import com.peluqueria.estructura.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final MascotaService mascotaService;

    public CitaController(CitaService citaService, UsuarioService usuarioService, MascotaService mascotaService) {
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.mascotaService = mascotaService;
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
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Mascota mascota = mascotaService.obtenerMascotaPorId(cita.getMascota().getId()).orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
            if (mascota.getCliente() == null || mascota.getCliente().getUsuario() == null) {
                throw new RuntimeException("La mascota no tiene un cliente o usuario asociado.");
            }
            if (usuario.getRol().equals("ADMIN") || mascota.getCliente().getUsuario().getId().equals(usuario.getId())) {
                cita.setMascota(mascota);
                return ResponseEntity.ok(citaService.createCita(cita));
            } else {
                return ResponseEntity.status(403).body("No estás autorizado para crear esta cita.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
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

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarCita(@RequestBody Cita cita) {
        String mensaje = citaService.registrarCita(cita.getMascota().getId(), cita.getServicio().getId(), cita.getFecha(), cita.getHora(), cita.getEstado());
        return ResponseEntity.ok(mensaje);
    }
}

