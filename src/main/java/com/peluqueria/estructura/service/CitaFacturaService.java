package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.repository.CitaRepository;
import com.peluqueria.estructura.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class CitaFacturaService {

    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;

    @Autowired
    public CitaFacturaService(CitaRepository citaRepository, ServicioRepository servicioRepository) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
    }
    
    /**
     * Obtiene una cita y verifica que no haya sido facturada
     */
    public Cita obtenerCitaParaFacturar(String citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
        
        if (cita.isFacturada()) {
            throw new RuntimeException("La cita ya ha sido facturada");
        }
        
        return cita;
    }
    
    /**
     * Crea un detalle de factura a partir de una cita
     */
    public DetalleFactura crearDetalleServicioDesdeCita(Cita cita) {
        Servicio servicio = cita.getServicio();
        
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(UUID.randomUUID().toString());
        detalle.setServicioId(servicio.getId());
        detalle.setServicioNombre(servicio.getNombre());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(servicio.getPrecio());
        detalle.setSubtotal(servicio.getPrecio());
        
        return detalle;
    }
    
    /**
     * Marca una cita como facturada, asociándola con una factura
     */
    @Transactional
    public void marcarCitaFacturada(String citaId, String facturaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
        
        cita.setFacturada(true);
        cita.setFacturaId(facturaId);
        citaRepository.save(cita);
    }
}
