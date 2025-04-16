package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}


