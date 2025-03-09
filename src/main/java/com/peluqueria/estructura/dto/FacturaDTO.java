package com.peluqueria.estructura.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FacturaDTO {
    private Long id;
    private String clienteNombre;
    private String clienteEmail;
    private LocalDateTime fecha;
    private BigDecimal total;
    private List<DetalleFacturaDTO> detalles;
}
