package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Document(collection = "servicios")
public class Servicio {

    @Id
    private String id;

    private String nombre;
    private int duracion;
    private BigDecimal precio;
}


