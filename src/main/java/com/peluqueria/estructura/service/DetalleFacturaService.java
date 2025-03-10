package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.repository.DetalleFacturaRepository;
import com.peluqueria.estructura.repository.FacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturaRepository facturaRepository;

    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository, FacturaRepository facturaRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturaRepository = facturaRepository;
    }

    public List<DetalleFacturaDTO> getAllDetalles(Long facturaId) {
        return detalleFacturaRepository.findByFacturaId(facturaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DetalleFacturaDTO getDetalleById(Long facturaId, Long detalleId) {
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        return convertToDTO(detalle);
    }

    public DetalleFacturaDTO createDetalle(Long facturaId, DetalleFacturaDTO detalleDTO) {
        DetalleFactura detalle = convertToEntity(detalleDTO);
        detalle.setFactura(facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada")));
        return convertToDTO(detalleFacturaRepository.save(detalle));
    }

    public DetalleFacturaDTO updateDetalle(Long facturaId, Long detalleId, DetalleFacturaDTO detalleDTO) {
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setSubtotal(detalleDTO.getSubtotal());
        return convertToDTO(detalleFacturaRepository.save(detalle));
    }

    public void deleteDetalle(Long facturaId, Long detalleId) {
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        detalleFacturaRepository.delete(detalle);
    }

    private DetalleFacturaDTO convertToDTO(DetalleFactura detalle) {
        DetalleFacturaDTO detalleDTO = new DetalleFacturaDTO();
        detalleDTO.setId(detalle.getId());
        detalleDTO.setProductoId(detalle.getProducto().getId());
        detalleDTO.setServicioId(detalle.getServicio().getId());
        detalleDTO.setCantidad(detalle.getCantidad());
        detalleDTO.setSubtotal(detalle.getSubtotal());
        return detalleDTO;
    }

    private DetalleFactura convertToEntity(DetalleFacturaDTO detalleDTO) {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setSubtotal(detalleDTO.getSubtotal());
        return detalle;
    }
}

