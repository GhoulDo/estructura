package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Document(collection = "productos")
public class Producto {

    @Id
    private String id;

    private String nombre;
    private String tipo;
    private BigDecimal precio;
    private int stock;
    private String descripcion;
    private String estado;
}
