package com.peluqueria.estructura.config;

import org.springframework.http.HttpMethod;
import com.peluqueria.estructura.security.JwtAuthenticationEntryPoint;
import com.peluqueria.estructura.config.JwtAuthenticationFilter;
import com.peluqueria.estructura.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioService usuarioService;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          UsuarioService usuarioService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.usuarioService = usuarioService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas para autenticación
                .requestMatchers("/api/auth/**").permitAll()
                
                // Permisos para servicios - permitir GET, PUT, PATCH para CLIENTE y ADMIN
                .requestMatchers(HttpMethod.GET, "/api/servicios/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/servicios/**").hasAnyRole("CLIENTE", "ADMIN")
                // Solo ADMIN puede realizar operaciones POST y DELETE
                .requestMatchers(HttpMethod.POST, "/api/servicios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasRole("ADMIN")
                
                // Permitir GET, PUT, PATCH para CLIENTE en todas las rutas API
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("CLIENTE", "ADMIN")
                
                // Permitir búsqueda por ID para CLIENTE (patrón específico)
                .requestMatchers(HttpMethod.GET, "/api/**/[0-9]+").hasAnyRole("CLIENTE", "ADMIN")
                
                // Solo ADMIN puede usar POST y DELETE en las rutas API generales
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                
                // Rutas específicas de admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}