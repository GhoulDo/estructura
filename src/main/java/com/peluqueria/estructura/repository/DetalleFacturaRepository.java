package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {
    List<DetalleFactura> findByFacturaId(Long facturaId);
    Optional<DetalleFactura> findByIdAndFacturaId(Long id, Long facturaId);
}


