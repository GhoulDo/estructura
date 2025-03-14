package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    Optional<Cliente> findByUsuarioUsername(String username);
}

