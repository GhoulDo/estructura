package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Document(collection = "citas")
public class Cita {

    @Id
    private String id;

    @DBRef
    private Mascota mascota;

    @DBRef
    private Servicio servicio;

    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    
    // Nuevos campos para facturación
    private boolean facturada = false;
    private String facturaId;
}

