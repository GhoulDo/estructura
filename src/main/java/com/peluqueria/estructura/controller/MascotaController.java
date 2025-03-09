package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.ClienteService;
import com.peluqueria.estructura.service.MascotaService;
import com.peluqueria.estructura.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;

    public MascotaController(MascotaService mascotaService, UsuarioService usuarioService, ClienteService clienteService) {
        this.mascotaService = mascotaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas(Authentication authentication) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            if (usuario.getRol().equals("ADMIN")) {
                return ResponseEntity.ok(mascotaService.getAllMascotas());
            } else {
                return ResponseEntity.ok(mascotaService.getMascotasByUsuarioId(usuario.getId()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMascotaById(@PathVariable Long id, Authentication authentication) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Mascota mascota = mascotaService.obtenerMascotaPorId(id).orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
            if (usuario.getRol().equals("ADMIN") || mascota.getCliente().getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.ok(mascota);
            } else {
                return ResponseEntity.status(403).body("No estás autorizado para ver esta mascota.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createMascota(@RequestBody Mascota mascota, Authentication authentication) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Cliente cliente = clienteService.getClientesByUsuarioId(usuario.getId()).stream().findFirst().orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            mascota.setCliente(cliente);
            return ResponseEntity.ok(mascotaService.createMascota(mascota));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMascota(@PathVariable Long id, @RequestBody Mascota mascota, Authentication authentication) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Mascota existingMascota = mascotaService.obtenerMascotaPorId(id).orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
            if (usuario.getRol().equals("ADMIN") || existingMascota.getCliente().getUsuario().getId().equals(usuario.getId())) {
                existingMascota.setNombre(mascota.getNombre());
                existingMascota.setTipo(mascota.getTipo());
                existingMascota.setRaza(mascota.getRaza());
                existingMascota.setEdad(mascota.getEdad());
                return ResponseEntity.ok(mascotaService.guardarMascota(existingMascota));
            } else {
                return ResponseEntity.status(403).body("No estás autorizado para actualizar esta mascota.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMascota(@PathVariable Long id, Authentication authentication) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Mascota mascota = mascotaService.obtenerMascotaPorId(id).orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
            if (usuario.getRol().equals("ADMIN") || mascota.getCliente().getUsuario().getId().equals(usuario.getId())) {
                mascotaService.eliminarMascota(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(403).body("No estás autorizado para eliminar esta mascota.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}

