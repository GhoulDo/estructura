package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.dto.FacturaDTO;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public ResponseEntity<List<FacturaDTO>> getAllFacturas() {
        List<Factura> facturas = facturaService.getAllFacturas();
        List<FacturaDTO> facturaDTOs = facturas.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(facturaDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> getFacturaById(@PathVariable Long id) {
        Factura factura = facturaService.getFacturaById(id);
        FacturaDTO facturaDTO = convertToDTO(factura);
        return ResponseEntity.ok(facturaDTO);
    }

    @PostMapping
    public ResponseEntity<Factura> createFactura(@RequestBody Factura factura) {
        return ResponseEntity.ok(facturaService.createFactura(factura));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotalFactura(@PathVariable Long id) {
        BigDecimal total = facturaService.calcularTotalFactura(id);
        return ResponseEntity.ok(total);
    }

    private FacturaDTO convertToDTO(Factura factura) {
        FacturaDTO facturaDTO = new FacturaDTO();
        facturaDTO.setId(factura.getId());
        facturaDTO.setClienteNombre(factura.getCliente().getNombre());
        facturaDTO.setClienteEmail(factura.getCliente().getEmail());
        facturaDTO.setFecha(factura.getFecha());
        facturaDTO.setTotal(factura.getTotal());

        List<DetalleFacturaDTO> detallesDTO = factura.getDetalles().stream().map(this::convertDetalleToDTO).collect(Collectors.toList());
        facturaDTO.setDetalles(detallesDTO);

        return facturaDTO;
    }

    private DetalleFacturaDTO convertDetalleToDTO(DetalleFactura detalle) {
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
}

