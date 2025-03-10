package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Intenta buscar primero por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(usernameOrEmail);
        
        // Si no encuentra por email, intenta por username
        if (!usuarioOpt.isPresent()) {
            usuarioOpt = usuarioRepository.findByUsername(usernameOrEmail);
        }
        
        Usuario usuario = usuarioOpt.orElseThrow(() -> 
                new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail));
        
        // Crear autoridades basadas en el rol del usuario
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Asegurarse que el rol tenga el prefijo ROLE_
        if (usuario.getRol() != null) {
            String rol = usuario.getRol().toUpperCase();
            if (!rol.startsWith("ROLE_")) {
                rol = "ROLE_" + rol;
            }
            authorities.add(new SimpleGrantedAuthority(rol));
        }
        
        return new org.springframework.security.core.userdetails.User(
            usuario.getUsername(),  // Usar username en vez de email
            usuario.getPassword(), 
            authorities
        );
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}