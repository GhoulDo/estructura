
package com.peluqueria.estructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class CorsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://peluqueriacanina-app.onrender.com,https://spapets.vercel.app}")
    private String allowedOrigins;
    

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configurar orígenes permitidos explícitamente
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            config.addAllowedOrigin(origin.trim());
            logger.info("CORS: Permitiendo origen: {}", origin.trim());
        }

        // También permitimos cualquier origen como respaldo
        config.addAllowedOriginPattern("*");
        logger.info("CORS: También permitiendo cualquier origen con patrón comodín");

        // Configurar credenciales - ahora permitimos credenciales
        config.setAllowCredentials(true);
        logger.info("CORS: Configurado para permitir credenciales");

        // Permitir todos los encabezados
        config.addAllowedHeader("*");

        // Permitir todos los métodos HTTP necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Exponer encabezados específicos al cliente
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.addExposedHeader("Access-Control-Allow-Credentials");

        // Tiempo de caché para respuestas preflight (en segundos)
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        logger.info("Configuración CORS mejorada aplicada a todas las rutas");
        return new CorsFilter(source);
    }
}