package com.peluqueria.estructura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;





@Getter
@Setter
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    
    @Indexed(unique = true)
    private String email;
    
    private String rol; // Puede ser ADMIN o CLIENTE
}


