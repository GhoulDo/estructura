package com.peluqueria.estructura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String username;
    private String email;
    private String password; // Nueva contraseña (opcional)
    private String currentPassword; // Contraseña actual para verificación

    // Campos adicionales para el cliente
    private String nombre;
    private String telefono;
    private String direccion;
}
