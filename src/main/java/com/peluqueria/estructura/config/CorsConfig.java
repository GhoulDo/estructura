package com.peluqueria.estructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // IMPORTANTE: Configurando correctamente para evitar errores de CORS

        // Permitir orígenes específicos en lugar del wildcard con credentials
        config.addAllowedOrigin("http://localhost:3000"); // Para desarrollo React
        config.addAllowedOrigin("http://localhost:5173"); // Para desarrollo Vite
        config.addAllowedOrigin("https://peluqueriacanina-app.onrender.com"); // Para producción

        // O para permitir todos los orígenes - usar esta opción SÓLO si tienes
        // configurado setAllowCredentials(false)
        // config.addAllowedOriginPattern("*");

        // Permitir credenciales (cookies, encabezados de autorización)
        // NOTA: Si esto es true, no puedes usar addAllowedOriginPattern("*")
        config.setAllowCredentials(true);

        // Permitir todos los headers y métodos que necesites
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Configurar headers expuestos (importantes para autenticación)
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");

        // Hacer que el navegador recuerde la respuesta CORS por más tiempo
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
