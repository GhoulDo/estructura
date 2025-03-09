package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.service.DetalleFacturaService;
import com.peluqueria.estructura.service.FacturaService;
import com.peluqueria.estructura.service.ProductoService;
import com.peluqueria.estructura.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facturas/{facturaId}/detalles")
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;
    private final FacturaService facturaService;
    private final ProductoService productoService;
    private final ServicioService servicioService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService, FacturaService facturaService, ProductoService productoService, ServicioService servicioService) {
        this.detalleFacturaService = detalleFacturaService;
        this.facturaService = facturaService;
        this.productoService = productoService;
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<List<DetalleFacturaDTO>> getAllDetalles(@PathVariable Long facturaId) {
        Factura factura = facturaService.getFacturaById(facturaId);
        List<DetalleFacturaDTO> detallesDTO = factura.getDetalles().stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(detallesDTO);
    }

    @GetMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> getDetalleById(@PathVariable Long facturaId, @PathVariable Long detalleId) {
        DetalleFactura detalle = detalleFacturaService.getDetalleById(detalleId).orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        return ResponseEntity.ok(convertToDTO(detalle));
    }

    @PostMapping
    public ResponseEntity<DetalleFacturaDTO> createDetalle(@PathVariable Long facturaId, @RequestBody DetalleFacturaDTO detalleDTO) {
        Factura factura = facturaService.getFacturaById(facturaId);
        DetalleFactura detalle = convertToEntity(detalleDTO);
        detalle.setFactura(factura);

        if (detalleDTO.getProductoId() != null) {
            Producto producto = productoService.getProductoById(detalleDTO.getProductoId()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            detalle.setProducto(producto);
        }

        if (detalleDTO.getServicioId() != null) {
            Servicio servicio = servicioService.getServicioById(detalleDTO.getServicioId()).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            detalle.setServicio(servicio);
        }

        DetalleFactura savedDetalle = detalleFacturaService.createDetalle(detalle);
        return ResponseEntity.ok(convertToDTO(savedDetalle));
    }

    @PutMapping("/{detalleId}")
    public ResponseEntity<DetalleFacturaDTO> updateDetalle(@PathVariable Long facturaId, @PathVariable Long detalleId, @RequestBody DetalleFacturaDTO detalleDTO) {
        DetalleFactura detalle = detalleFacturaService.getDetalleById(detalleId).orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setSubtotal(detalleDTO.getSubtotal());

        if (detalleDTO.getProductoId() != null) {
            Producto producto = productoService.getProductoById(detalleDTO.getProductoId()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            detalle.setProducto(producto);
        }

        if (detalleDTO.getServicioId() != null) {
            Servicio servicio = servicioService.getServicioById(detalleDTO.getServicioId()).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            detalle.setServicio(servicio);
        }

        DetalleFactura updatedDetalle = detalleFacturaService.updateDetalle(detalle);
        return ResponseEntity.ok(convertToDTO(updatedDetalle));
    }

    @DeleteMapping("/{detalleId}")
    public ResponseEntity<Void> deleteDetalle(@PathVariable Long facturaId, @PathVariable Long detalleId) {
        detalleFacturaService.deleteDetalle(detalleId);
        return ResponseEntity.noContent().build();
    }

    private DetalleFacturaDTO convertToDTO(DetalleFactura detalle) {
        DetalleFacturaDTO detalleDTO = new DetalleFacturaDTO();
        detalleDTO.setId(detalle.getId());
        if (detalle.getProducto() != null) {
            detalleDTO.setProductoId(detalle.getProducto().getId());
        }
        if (detalle.getServicio() != null) {
            detalleDTO.setServicioId(detalle.getServicio().getId());
        }
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
