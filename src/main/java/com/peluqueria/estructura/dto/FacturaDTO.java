// src/main/java/com/peluqueria/estructura/dto/FacturaDTO.java
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
    private Long clienteId;
    private String clienteNombre;
    private LocalDateTime fecha;
    private BigDecimal total;
    // No exponemos la lista completa de detalles por defecto
}