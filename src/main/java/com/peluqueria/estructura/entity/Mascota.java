package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "mascotas")
public class Mascota {

    @Id
    private String id;

    private String nombre;
    private String tipo; // Antes especie
    private String raza;
    private int edad;

    @DBRef
    private Cliente cliente;
}
