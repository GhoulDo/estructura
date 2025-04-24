package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CalculadoraFacturaService {

    /**
     * Calcula el subtotal de un detalle de factura
     */
    public BigDecimal calcularSubtotal(DetalleFactura detalle) {
        if (detalle.getPrecioUnitario() == null) {
            return BigDecimal.ZERO;
        }

        return detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad()));
    }

    /**
     * Calcula el total de una factura sumando todos sus detalles
     */
    public BigDecimal calcularTotalFactura(List<DetalleFactura> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return detalles.stream()
                .map(detalle -> {
                    if (detalle.getSubtotal() != null) {
                        return detalle.getSubtotal();
                    } else {
                        return calcularSubtotal(detalle);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Actualiza los cálculos de una factura completa
     */
    public Factura recalcularFactura(Factura factura) {
        if (factura.getDetalles() != null) {
            // Recalcular cada subtotal
            factura.getDetalles().forEach(detalle -> {
                if (detalle.getSubtotal() == null || detalle.getPrecioUnitario() == null) {
                    detalle.setSubtotal(calcularSubtotal(detalle));
                }
            });

            // Actualizar total
            factura.setTotal(calcularTotalFactura(factura.getDetalles()));
        } else {
            factura.setTotal(BigDecimal.ZERO);
        }

        return factura;
    }
}
