package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.service.MascotaService;
import com.peluqueria.estructura.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private static final Logger logger = LoggerFactory.getLogger(MascotaController.class);
    private final MascotaService mascotaService;
    private final ClienteService clienteService;
    private final ObjectMapper objectMapper;

    @Value("${api.base-url:}")
    private String apiBaseUrl; // URL base de la API, puede ser configurada en properties

    public MascotaController(MascotaService mascotaService, ClienteService clienteService, ObjectMapper objectMapper) {
        this.mascotaService = mascotaService;
        this.clienteService = clienteService;
        this.objectMapper = objectMapper;
    }

    private String buildFotoUrl(String mascotaId, HttpServletRequest request) {
        // Si la URL base está configurada en las propiedades, usarla
        if (apiBaseUrl != null && !apiBaseUrl.isEmpty()) {
            return apiBaseUrl + "/api/mascotas/" + mascotaId + "/foto";
        }

        // Si no, construir la URL basada en la petición actual
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        // Construir la URL completa
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // Añadir el puerto solo si no es el puerto por defecto (80 para HTTP, 443 para
        // HTTPS)
        if ((serverPort != 80 && "http".equals(scheme)) ||
                (serverPort != 443 && "https".equals(scheme))) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        if (!contextPath.endsWith("/")) {
            url.append("/");
        }
        url.append("api/mascotas/").append(mascotaId).append("/foto");

        return url.toString();
    }

    /**
     * Establece la URL de foto para una mascota si tiene foto
     */
    private void setFotoUrl(Mascota mascota, HttpServletRequest request) {
        if (mascota.getTieneFoto()) {
            mascota.setFotoUrl(buildFotoUrl(mascota.getId(), request));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllMascotas(Authentication authentication, HttpServletRequest request) {
        logger.info("Petición recibida para obtener todas las mascotas");
        try {
            // Verificar si hay autenticación
            if (authentication == null) {
                logger.error("Error: Authentication es null");
                Map<String, String> error = new HashMap<>();
                error.put("error", "No hay autenticación");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String username = authentication.getName();
            logger.info("Usuario autenticado: {}", username);

            // Intento obtener las mascotas
            List<Mascota> mascotas = mascotaService.findByClienteUsuarioUsername(username);

            if (mascotas == null) {
                logger.warn("findByClienteUsuarioUsername devolvió null para {}", username);
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Establecer la URL de la foto para cada mascota
            mascotas.forEach(mascota -> setFotoUrl(mascota, request));

            logger.info("Encontradas {} mascotas para el usuario {}", mascotas.size(), username);
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            logger.error("Error al obtener mascotas: {} - Causa: {}", e.getMessage(), e.getCause(), e);

            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("mensaje", e.getMessage());
            error.put("causa", e.getCause() != null ? e.getCause().getMessage() : "Desconocida");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable String id, Authentication authentication,
            HttpServletRequest request) {
        logger.info("Petición recibida para obtener la mascota con ID: {} del usuario: {}", id,
                authentication.getName());
        Optional<Mascota> mascotaOpt = mascotaService.findByIdAndClienteUsuarioUsername(id, authentication.getName());
        if (mascotaOpt.isPresent()) {
            Mascota mascota = mascotaOpt.get();
            // Establecer la URL de la foto si tiene foto
            setFotoUrl(mascota, request);
            logger.info("Mascota encontrada: {}", mascota);
            return ResponseEntity.ok(mascota);
        } else {
            logger.warn("Mascota con ID: {} no encontrada para el usuario: {}", id, authentication.getName());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createMascota(
            @RequestParam(value = "mascota", required = false) String mascotaJson,
            @RequestPart(value = "mascota", required = false) Mascota mascotaDirecta,
            @RequestBody(required = false) Mascota mascotaBody,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication,
            HttpServletRequest request) {

        logger.info("Petición recibida para crear mascota. Content-Type: {}",
                (mascotaJson != null) ? "form-data con JSON string"
                        : (mascotaDirecta != null) ? "form-data con objeto"
                                : (mascotaBody != null) ? "application/json" : "desconocido");

        Mascota mascota = null;

        try {
            // Determinar cuál de los parámetros contiene los datos de la mascota
            if (mascotaJson != null && !mascotaJson.isEmpty()) {
                // Si se envió como string JSON dentro de un form-data
                logger.debug("Procesando mascota desde JSON string en form-data: {}", mascotaJson);
                mascota = objectMapper.readValue(mascotaJson, Mascota.class);
            } else if (mascotaDirecta != null) {
                // Si Spring pudo deserializar directamente el JSON a un objeto Mascota
                logger.debug("Procesando mascota desde objeto directo en form-data");
                mascota = mascotaDirecta;
            } else if (mascotaBody != null) {
                // Si se envió como JSON en el cuerpo de la petición
                logger.debug("Procesando mascota desde body JSON");
                mascota = mascotaBody;
            } else {
                logger.error("No se recibieron datos de mascota en ningún formato conocido");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos de mascota no encontrados",
                                "mensaje", "Debes enviar la mascota como JSON o como parte de un form-data"));
            }

            // Continuar con el procesamiento normal
            return procesarCreacionMascota(mascota, foto, authentication, request);

        } catch (Exception e) {
            logger.error("Error al procesar los datos de la mascota: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al procesar los datos de la mascota");
            error.put("mensaje", e.getMessage());
            error.put("detalleError", e.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createMascotaJson(
            @RequestBody Mascota mascotaBody,
            Authentication authentication,
            HttpServletRequest request) {

        logger.info("Petición JSON recibida para crear mascota sin foto");
        return procesarCreacionMascota(mascotaBody, null, authentication, request);
    }

    @PostMapping("/con-foto")
    public ResponseEntity<?> createMascotaConFoto(
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication,
            HttpServletRequest request) {

        logger.info("Petición multipart recibida para crear mascota con foto");
        logger.debug("Datos recibidos - mascotaJson: {}, foto: {}",
                mascotaJson,
                (foto != null ? foto.getOriginalFilename() + ", " + foto.getContentType() : "null"));

        try {
            // Deserializar el JSON string a objeto Mascota
            Mascota mascota = objectMapper.readValue(mascotaJson, Mascota.class);
            return procesarCreacionMascota(mascota, foto, authentication, request);
        } catch (Exception e) {
            logger.error("Error al procesar los datos multipart: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al procesar los datos multipart");
            error.put("mensaje", e.getMessage());
            error.put("detalleError", e.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    private ResponseEntity<?> procesarCreacionMascota(
            Mascota mascota,
            MultipartFile foto,
            Authentication authentication,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Verificar y registrar los datos recibidos
            logger.debug("Datos de mascota recibidos: nombre={}, tipo={}, raza={}, edad={}",
                    mascota.getNombre(), mascota.getTipo(), mascota.getRaza(), mascota.getEdad());

            // 2. Obtener el cliente asociado al usuario autenticado
            Cliente cliente = null;
            try {
                cliente = clienteService.findByUsuarioUsername(authentication.getName());
                logger.info("Cliente encontrado: {} con ID: {}", cliente.getNombre(), cliente.getId());
            } catch (Exception e) {
                logger.error("Error al encontrar el cliente del usuario {}: {}", authentication.getName(),
                        e.getMessage());
                response.put("error", "Error al encontrar el cliente asociado al usuario");
                response.put("mensaje", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 3. Asociar el cliente a la mascota
            mascota.setCliente(cliente);

            // 4. Guardar la mascota
            Mascota savedMascota = mascotaService.save(mascota);
            logger.info("Mascota creada con éxito: ID={}, Nombre={}", savedMascota.getId(), savedMascota.getNombre());

            // 5. Procesar la foto si existe
            boolean fotoGuardada = false;
            if (foto != null && !foto.isEmpty()) {
                try {
                    byte[] fotoBytes = foto.getBytes();
                    logger.info("Foto recibida: {} bytes, contentType: {}",
                            fotoBytes.length, foto.getContentType());

                    // Verificar tamaño razonable
                    if (fotoBytes.length > 5 * 1024 * 1024) { // 5MB
                        logger.warn("La foto es demasiado grande: {} bytes", fotoBytes.length);
                        response.put("advertencia", "La foto es demasiado grande y ha sido ignorada");
                    } else {
                        mascotaService.saveFoto(savedMascota.getId(), fotoBytes);
                        fotoGuardada = true;
                        logger.info("Foto guardada para la mascota con ID: {}", savedMascota.getId());
                    }
                } catch (IOException e) {
                    logger.error("Error al procesar bytes de la foto: {}", e.getMessage());
                    response.put("advertencia", "No se pudo procesar la foto: " + e.getMessage());
                }
            }

            // 6. Establecer la URL de la foto si se guardó correctamente
            if (fotoGuardada) {
                savedMascota.setFotoUrl(buildFotoUrl(savedMascota.getId(), request));
            }

            // 7. Devolver la respuesta exitosa
            response.put("mascota", savedMascota);
            response.put("mensaje", "Mascota creada exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // Error general
            logger.error("Error al crear la mascota: {} - Causa: {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "desconocida", e);

            response.put("error", "Error al crear la mascota");
            response.put("mensaje", e.getMessage());
            response.put("causa", e.getCause() != null ? e.getCause().getMessage() : "Desconocida");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Asumimos JPEG, podría ser dinámico según el tipo de imagen
                    .body(foto);
        } else {
            logger.warn("Foto no encontrada para la mascota con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/diagnostico")
    public ResponseEntity<?> diagnosticarMultipart(
            @RequestParam(value = "mascota", required = false) String mascotaJson,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestHeader Map<String, String> headers,
            Authentication authentication) {

        Map<String, Object> diagnostico = new HashMap<>();
        diagnostico.put("headers_recibidos", headers);

        // Diagnosticar los datos recibidos
        if (mascotaJson != null) {
            diagnostico.put("mascota_recibida", mascotaJson);
            diagnostico.put("longitud_json", mascotaJson.length());
        } else {
            diagnostico.put("mascota_recibida", "No se recibió ningún JSON de mascota");
        }

        // Diagnosticar si se recibió un archivo
        if (foto != null) {
            Map<String, Object> fotoInfo = new HashMap<>();
            fotoInfo.put("nombre", foto.getOriginalFilename());
            fotoInfo.put("tipo", foto.getContentType());
            fotoInfo.put("tamaño", foto.getSize());
            fotoInfo.put("está_vacío", foto.isEmpty());
            diagnostico.put("foto_info", fotoInfo);
        } else {
            diagnostico.put("foto_info", "No se recibió ningún archivo");
        }

        // Incluir información de autenticación
        if (authentication != null) {
            diagnostico.put("usuario_autenticado", authentication.getName());
        }

        return ResponseEntity.ok(diagnostico);
    }
}
