package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.service.MascotaService;
import com.peluqueria.estructura.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private static final Logger logger = LoggerFactory.getLogger(MascotaController.class);
    private final MascotaService mascotaService;
    private final ClienteService clienteService;

    public MascotaController(MascotaService mascotaService, ClienteService clienteService) {
        this.mascotaService = mascotaService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas(Authentication authentication) {
        logger.info("Petición recibida para obtener todas las mascotas del usuario: {}", authentication.getName());
        try {
            List<Mascota> mascotas = mascotaService.findByClienteUsuarioUsername(authentication.getName());
            logger.info("Encontradas {} mascotas para el usuario {}", mascotas.size(), authentication.getName());
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            logger.error("Error al obtener mascotas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable String id, Authentication authentication) {
        logger.info("Petición recibida para obtener la mascota con ID: {} del usuario: {}", id,
                authentication.getName());
        Optional<Mascota> mascota = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        if (mascota.isPresent()) {
            logger.info("Mascota encontrada: {}", mascota.get());
            return ResponseEntity.ok(mascota.get());
        } else {
            logger.warn("Mascota con ID: {} no encontrada para el usuario: {}", id, authentication.getName());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Mascota> createMascota(@RequestPart("mascota") Mascota mascota,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {
        logger.info("Petición recibida para crear una nueva mascota para el usuario: {}", authentication.getName());
        try {
            mascota.setCliente(clienteService.findByUsuarioUsername(authentication.getName()));
            Mascota savedMascota = mascotaService.save(mascota);
            logger.info("Mascota creada con éxito: {}", savedMascota);

            if (foto != null && !foto.isEmpty()) {
                mascotaService.saveFoto(savedMascota.getId(), foto.getBytes());
                logger.info("Foto guardada para la mascota con ID: {}", savedMascota.getId());
            }

            return ResponseEntity.ok(savedMascota);
        } catch (Exception e) {
            logger.error("Error al crear la mascota: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> updateMascota(@PathVariable String id, @RequestBody Mascota mascota,
            Authentication authentication) {
        logger.info("Petición recibida para actualizar la mascota con ID: {} del usuario: {}", id,
                authentication.getName());
        Optional<Mascota> existingMascota = mascotaService.findByIdAndClienteUsuarioUsername(id,
                authentication.getName());
        if (existingMascota.isPresent()) {
            mascota.setId(id);
            Mascota updatedMascota = mascotaService.save(mascota);
            logger.info("Mascota actualizada con éxito: {}", updatedMascota);
            return ResponseEntity.ok(updatedMascota);
        } else {
            logger.warn("Mascota con ID: {} no encontrada para el usuario: {}", id, authentication.getName());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable String id, Authentication authentication) {
        logger.info("Petición recibida para eliminar la mascota con ID: {} del usuario: {}", id,
                authentication.getName());
        Optional<Mascota> mascota = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        if (mascota.isPresent()) {
            mascotaService.deleteById(id);
            logger.info("Mascota con ID: {} eliminada con éxito", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Mascota con ID: {} no encontrada para el usuario: {}", id, authentication.getName());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<String> uploadFoto(@PathVariable String id, @RequestParam("foto") MultipartFile foto) {
        logger.info("Petición recibida para subir una foto para la mascota con ID: {}", id);
        try {
            mascotaService.saveFoto(id, foto.getBytes());
            logger.info("Foto subida correctamente para la mascota con ID: {}", id);
            return ResponseEntity.ok("Foto subida correctamente");
        } catch (Exception e) {
            logger.error("Error al subir la foto para la mascota con ID: {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al subir la foto: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable String id) {
        logger.info("Petición recibida para obtener la foto de la mascota con ID: {}", id);
        byte[] foto = mascotaService.getFoto(id);
        if (foto != null) {
            logger.info("Foto obtenida correctamente para la mascota con ID: {}", id);
            return ResponseEntity.ok(foto);
        } else {
            logger.warn("Foto no encontrada para la mascota con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
