package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private String descripcion;

    @DBRef
    private Cliente cliente;

    @JsonIgnore // Ignorar este campo en la serialización JSON
    @Field
    private byte[] foto; // Campo para almacenar la foto de la mascota en formato binario
    
    @Transient // Campo no persistido en la base de datos
    private String fotoUrl; // URL para acceder a la foto

    // Campo virtual para indicar si tiene foto
    public boolean getTieneFoto() {
        return foto != null && foto.length > 0;
    }
}
