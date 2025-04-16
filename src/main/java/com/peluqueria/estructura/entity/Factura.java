package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "facturas")
public class Factura {

    @Id
    private String id;

    @DBRef
    private Cliente cliente;

    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado; // PENDIENTE, PAGADA, CANCELADA, etc.

    // En MongoDB podemos mantener los detalles embebidos en lugar de usar DBRef
    // para mejorar el rendimiento de las consultas
    private List<DetalleFactura> detalles;
}


