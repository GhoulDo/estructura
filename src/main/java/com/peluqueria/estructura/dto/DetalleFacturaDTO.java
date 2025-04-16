package com.peluqueria.estructura.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleFacturaDTO {
    
    private String id;
    private String productoId;
    private String servicioId;
    private int cantidad;
    private BigDecimal subtotal;
    // Otros campos que puedan ser necesarios para la vista
}
