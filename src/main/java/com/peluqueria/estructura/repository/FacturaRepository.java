package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteId(Long clienteId);

    @Query(value = "SELECT calcular_total_factura(:facturaId)", nativeQuery = true)
    BigDecimal calcularTotalFactura(@Param("facturaId") Long facturaId);
}

