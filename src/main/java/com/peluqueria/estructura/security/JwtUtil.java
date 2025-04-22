package com.peluqueria.estructura.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final ConcurrentHashMap<String, String> tokenBlacklist = new ConcurrentHashMap<>();

    private Key getSignKey() {
        try {
            // Intenta usar decodificación base64 si la clave está en ese formato
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (Exception e) {
            logger.warn("Error al decodificar la clave secreta como Base64, utilizando bytes directos: {}",
                    e.getMessage());
            // Si falla, usa los bytes directamente (más seguro para claves con caracteres
            // especiales)
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error al extraer el usuario del token: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error al extraer la fecha de expiración: {}", e.getMessage());
            return new Date(); // Devuelve la fecha actual para que isTokenExpired devuelva true
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("El token JWT ha expirado.");
            throw new IllegalArgumentException("El token JWT ha expirado.");
        } catch (UnsupportedJwtException e) {
            logger.warn("El token JWT no es compatible.");
            throw new IllegalArgumentException("El token JWT no es compatible.");
        } catch (MalformedJwtException e) {
            logger.warn("El token JWT está malformado.");
            throw new IllegalArgumentException("El token JWT está malformado.");
        } catch (Exception e) {
            logger.error("Error al procesar el token JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Error al procesar el token JWT: " + e.getMessage());
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("Error al verificar la expiración del token: {}", e.getMessage());
            return true;
        }
    }

    public String generateToken(String username) {
        return createToken(new HashMap<>(), username);
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();

        // Añadir roles al token para autorización
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            logger.error("Error al extraer roles del token: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean isTokenValid(String token, String username) {
        if (tokenBlacklist.containsKey(username)) {
            return false;
        }
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Error al verificar validez del token: {}", e.getMessage());
            return false;
        }
    }

    public void invalidateToken(String username) {
        tokenBlacklist.put(username, "INVALIDATED");
    }
}