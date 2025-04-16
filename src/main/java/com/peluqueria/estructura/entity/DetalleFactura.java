package com.peluqueria.estructura.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
// No usamos @Document ya que será un documento embebido en Factura
public class DetalleFactura {
    
    private String id;
    
    // Referencia al ID del producto (si es un producto)
    private String productoId;
    private String productoNombre;
    
    // Referencia al ID del servicio (si es un servicio)
    private String servicioId;
    private String servicioNombre;
    
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
