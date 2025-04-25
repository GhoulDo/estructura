package com.peluqueria.estructura.dto;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String id;
    private String username;
    private String email;
    private String rol;

    // Datos del cliente si el usuario tiene un cliente asociado
    private String clienteId;
    private String nombre;
    private String telefono;
    private String direccion;

    // Constructor para crear el DTO a partir de un Usuario y opcionalmente un
    // Cliente
    public static UserProfileDTO fromEntities(Usuario usuario, Cliente cliente) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());

        if (cliente != null) {
            dto.setClienteId(cliente.getId());
            dto.setNombre(cliente.getNombre());
            dto.setTelefono(cliente.getTelefono());
            dto.setDireccion(cliente.getDireccion());
        }

        return dto;
    }
}
