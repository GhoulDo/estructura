package com.peluqueria.estructura.config;

import com.peluqueria.estructura.security.JwtUtil;
import com.peluqueria.estructura.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioService usuarioService) {
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain chain) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        
        // Log para debugging
        logger.info("Processing request: " + method + " " + requestURI);

        // Si es una ruta pública, continuamos sin verificar
        if (requestURI.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("No Authorization header or not Bearer token");
            chain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        
        logger.info("Token received for user: " + username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = usuarioService.loadUserByUsername(username);
                
                // Log para ver las autoridades asignadas
                logger.info("User authorities: " + userDetails.getAuthorities());

                if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                            );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.info("Authentication successful, user authenticated with authorities");
                } else {
                    logger.warn("Token validation failed");
                }
            } catch (Exception e) {
                logger.error("Error authenticating user: " + e.getMessage(), e);
            }
        }

        chain.doFilter(request, response);
    }
}