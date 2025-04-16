package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Document(collection = "clientes")
public class Cliente {

    @Id
    private String id;

    private String nombre;
    private String telefono;
    private String email;
    private String direccion;

    @DBRef
    private Usuario usuario;

    @DBRef
    private List<Mascota> mascotas;
}

