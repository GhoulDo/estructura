package com.peluqueria.estructura.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DetalleFacturaDTO {
    private Long id;
    private Long productoId;
    private Long servicioId;
    private int cantidad;
    private BigDecimal subtotal;
}
