package com.peluqueria.estructura.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "servicios") // Asegúrate de que el nombre de la tabla sea correcto
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int duracion;
    private BigDecimal precio;
}


