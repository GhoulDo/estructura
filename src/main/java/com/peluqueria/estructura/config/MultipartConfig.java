package com.peluqueria.estructura.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.File;

@Configuration
public class MultipartConfig {

    /**
     * Configura el resolver para manejar solicitudes multipart.
     * 
     * @return Un MultipartResolver configurado para la aplicación
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Configura los ajustes específicos para el manejo de multipart.
     * 
     * @return Una configuración multipart personalizada
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Establecer tamaños máximos de archivo y solicitud
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));

        // Establecer un umbral para cuando los archivos deben escribirse en disco
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));

        // Establecer una ubicación temporal para archivos (opcional)
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempDirectory = new File(tempDir);
        if (tempDirectory.exists()) {
            factory.setLocation(tempDir);
        }

        return factory.createMultipartConfig();
    }
}