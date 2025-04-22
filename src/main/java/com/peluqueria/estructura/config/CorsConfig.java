package com.peluqueria.estructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class CorsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://peluqueriacanina-app.onrender.com}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configuración para permitir solicitudes de cualquier origen
        config.addAllowedOriginPattern("*");
        logger.info("CORS configurado para permitir cualquier origen");

        // Permitir envío de credenciales (cookies, autenticación HTTP)
        config.setAllowCredentials(false);
        logger.info("CORS configurado con allowCredentials=false para compatibilidad con wildcard origins");

        // Permitir todos los encabezados
        config.addAllowedHeader("*");

        // Permitir todos los métodos HTTP necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Exponer encabezados específicos al cliente
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");

        // Tiempo de caché para respuestas preflight (en segundos)
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        logger.info("Configuración CORS aplicada a todas las rutas");
        return new CorsFilter(source);
    }
}
