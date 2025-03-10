package com.peluqueria.estructura.service;



import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.repository.DetalleFacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;

    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
    }

    public List<DetalleFactura> listarDetalles() {
        return detalleFacturaRepository.findAll();
    }

    public Optional<DetalleFactura> obtenerDetallePorId(Long id) {
        return detalleFacturaRepository.findById(id);
    }

    public DetalleFactura guardarDetalle(DetalleFactura detalleFactura) {
        return detalleFacturaRepository.save(detalleFactura);
    }

    public void eliminarDetalle(Long id) {
        detalleFacturaRepository.deleteById(id);
    }
}

