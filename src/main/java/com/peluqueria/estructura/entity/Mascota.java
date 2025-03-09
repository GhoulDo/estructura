package com.peluqueria.estructura.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "mascotas") // Asegúrate de que el nombre de la tabla sea correcto
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo;
    private String raza;
    private int edad;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
