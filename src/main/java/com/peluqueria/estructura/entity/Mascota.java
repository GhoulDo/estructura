package com.peluqueria.estructura.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo; // Antes especie
    private String raza;
    private int edad;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
