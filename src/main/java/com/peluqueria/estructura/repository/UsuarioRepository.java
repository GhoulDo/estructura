package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    // Búsqueda case-insensitive
    @Query("{'username': {$regex: '^?0$', $options: 'i'}}")
    Optional<Usuario> findByUsernameIgnoreCase(String username);
}
