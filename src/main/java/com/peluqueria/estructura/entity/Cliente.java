package com.peluqueria.estructura.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "clientes") // Asegúrate de que el nombre de la tabla sea correcto
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String telefono;
    private String email;
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}

