package com.peluqueria.estructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://peluqueriacanina-app.onrender.com}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configuración más permisiva para desarrollo
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Permitir credenciales - incompatible con comodines de origen en producción
        // Para producción, si se usa withCredentials: true en frontend, usar:
        // config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        // config.setAllowCredentials(true);

        // Para configuración actual donde frontend tiene withCredentials: false:
        config.setAllowCredentials(false);

        // Establecer tiempo de caché CORS para mejorar rendimiento (1 hora)
        config.setMaxAge(3600L);

        // Permitir todos los headers
        config.addAllowedHeader("*");

        // Permitir todos los métodos HTTP que necesites
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Exponer headers importantes para autenticación
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
