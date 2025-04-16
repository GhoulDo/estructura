package com.peluqueria.estructura.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final Instant startTime = Instant.now();
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Value("${spring.application.name:Peluquería SPA}")
    private String applicationName;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        
        // Información de timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        status.put("timestamp", now.format(formatter));
        
        // Información del entorno
        status.put("application", applicationName);
        status.put("environment", activeProfile);
        
        // Tiempo de actividad
        Duration uptime = Duration.between(startTime, Instant.now());
        long days = uptime.toDays();
        long hours = uptime.toHoursPart();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        
        status.put("uptime", String.format("%d días, %d horas, %d minutos, %d segundos", 
                                          days, hours, minutes, seconds));
        
        return ResponseEntity.ok(status);
    }
}
