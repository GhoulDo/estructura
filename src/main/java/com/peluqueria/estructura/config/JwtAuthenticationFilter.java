package com.peluqueria.estructura.config;

import com.peluqueria.estructura.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Registrar información de la solicitud para depuración
        logger.debug("Procesando solicitud: {} {}", request.getMethod(), request.getRequestURI());
        String authorizationHeader = request.getHeader("Authorization");
        logger.debug("Header de autorización: {}",
                authorizationHeader != null
                        ? (authorizationHeader.length() > 20 ? authorizationHeader.substring(0, 20) + "..."
                                : authorizationHeader)
                        : "no presente");

        String username = null;
        String jwt = null;

        // Extraer token del encabezado Authorization
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Token JWT válido. Usuario extraído: {}", username);
            } catch (Exception e) {
                logger.error("Error al extraer el usuario del token JWT: {}", e.getMessage());
            }
        } else {
            logger.debug("No se encontró un token JWT válido en la solicitud");
        }

        // Si se extrajo un nombre de usuario y no hay autenticación existente
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validar token
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Usuario autenticado: {} con roles: {}", username, userDetails.getAuthorities());
            } else {
                logger.warn("Token JWT no válido para el usuario: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}