// Este archivo ya no es necesario porque los detalles de factura están embebidos en la entidad Factura
// Puede eliminar este archivo o mantenerlo comentado como referencia

/*
package com.peluqueria.estructura.repository;

import com.peluqueria.estructura.entity.DetalleFactura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleFacturaRepository extends MongoRepository<DetalleFactura, String> {
    List<DetalleFactura> findByFacturaId(String facturaId);
    Optional<DetalleFactura> findByIdAndFacturaId(String id, String facturaId);
}
*/


