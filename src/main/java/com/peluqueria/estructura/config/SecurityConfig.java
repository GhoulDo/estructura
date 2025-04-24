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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioService usuarioService;
    private final CorsFilter corsFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UsuarioService usuarioService,
            CorsFilter corsFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.usuarioService = usuarioService;
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors() // Habilitar soporte CORS
                .and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()

                        // Usuarios
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")

                        // Clientes
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")

                        // Mascotas
                        .requestMatchers(HttpMethod.GET, "/api/mascotas/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/mascotas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/mascotas/con-foto").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/mascotas/diagnostico").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/mascotas/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/mascotas/**").hasRole("CLIENTE")

                        // Servicios
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/servicios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").hasRole("ADMIN")

                        // Productos
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                        // Citas
                        .requestMatchers(HttpMethod.GET, "/api/citas/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/citas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/citas/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/citas/**").hasRole("CLIENTE")

                        // Carrito de compras (nueva funcionalidad)
                        .requestMatchers("/api/carrito/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/carrito/checkout/resumen").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/carrito/checkout/confirmar").hasRole("CLIENTE")

                        // Facturas (principal)
                        .requestMatchers(HttpMethod.GET, "/api/facturas").hasAnyRole("ADMIN", "CLIENTE") // Ver facturas
                                                                                                         // (ambos)
                        .requestMatchers(HttpMethod.GET, "/api/facturas/**").hasAnyRole("ADMIN", "CLIENTE") // Ver
                                                                                                            // detalle
                                                                                                            // de
                                                                                                            // factura
                                                                                                            // (ambos)
                        .requestMatchers(HttpMethod.POST, "/api/facturas").hasAnyRole("ADMIN", "CLIENTE") // Crear
                                                                                                          // factura
                                                                                                          // (ambos)
                        .requestMatchers(HttpMethod.PUT, "/api/facturas/**").hasRole("ADMIN") // Actualizar factura
                                                                                              // (solo admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/facturas/**").hasRole("ADMIN") // Eliminar factura
                                                                                                 // (solo admin)
                        .requestMatchers(HttpMethod.GET, "/api/facturas/cliente/**").hasAnyRole("ADMIN", "CLIENTE")

                        // Detalles de factura
                        .requestMatchers(HttpMethod.GET, "/api/facturas/**/detalles").hasAnyRole("ADMIN", "CLIENTE") // Ver
                                                                                                                     // detalles
                                                                                                                     // (ambos)
                        .requestMatchers(HttpMethod.GET, "/api/facturas/**/detalles/**").hasAnyRole("ADMIN", "CLIENTE") // Ver
                                                                                                                        // detalle
                                                                                                                        // específico
                                                                                                                        // (ambos)
                        .requestMatchers(HttpMethod.POST, "/api/facturas/**/detalles").hasAnyRole("ADMIN", "CLIENTE") // Añadir
                                                                                                                      // detalle
                                                                                                                      // (ambos)
                        .requestMatchers(HttpMethod.PUT, "/api/facturas/**/detalles/**").hasRole("ADMIN") // Modificar
                                                                                                          // detalle
                                                                                                          // (solo
                                                                                                          // admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/facturas/**/detalles/**").hasRole("ADMIN") // Eliminar
                                                                                                             // detalle
                                                                                                             // (solo
                                                                                                             // admin)

                        // Facturación unificada
                        .requestMatchers(HttpMethod.POST, "/api/facturacion-unificada/facturar-cita/**")
                        .hasRole("ADMIN") // Facturar cita (solo admin)
                        .requestMatchers(HttpMethod.PUT, "/api/facturacion-unificada/agregar-productos/**")
                        .hasAnyRole("ADMIN", "CLIENTE") // Agregar productos (ambos)
                        .requestMatchers(HttpMethod.PUT, "/api/facturacion-unificada/pagar/**").hasRole("ADMIN") // Marcar
                                                                                                                 // como
                                                                                                                 // pagada
                                                                                                                 // (solo
                                                                                                                 // admin)
                        .requestMatchers(HttpMethod.GET, "/api/facturacion-unificada/cliente/**")
                        .hasAnyRole("ADMIN", "CLIENTE") // Ver facturas de cliente (ambos)

                        // Checkout (nueva funcionalidad para clientes)
                        .requestMatchers("/api/checkout/**").hasRole("CLIENTE")

                        // Logout protegido
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Aplicar el filtro CORS antes del filtro JWT
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
