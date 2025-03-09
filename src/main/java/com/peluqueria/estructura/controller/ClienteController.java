package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.service.ClienteService;
import com.peluqueria.estructura.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    public ClienteController(ClienteService clienteService, UsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClientes(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getRol().equals("ADMIN")) {
            return ResponseEntity.ok(clienteService.getAllClientes());
        } else {
            return ResponseEntity.ok(clienteService.getClientesByUsuarioId(usuario.getId()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClienteById(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente cliente = clienteService.getClienteById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (usuario.getRol().equals("ADMIN") || cliente.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.ok(cliente);
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para ver este cliente.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente cliente, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getRol().equals("ADMIN") || cliente.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.ok(clienteService.createCliente(cliente));
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para crear este cliente.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente existingCliente = clienteService.getClienteById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (usuario.getRol().equals("ADMIN") || existingCliente.getUsuario().getId().equals(usuario.getId())) {
            existingCliente.setNombre(cliente.getNombre());
            existingCliente.setTelefono(cliente.getTelefono());
            existingCliente.setEmail(cliente.getEmail());
            existingCliente.setDireccion(cliente.getDireccion());
            existingCliente.setUsuario(cliente.getUsuario());
            return ResponseEntity.ok(clienteService.guardarCliente(existingCliente));
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para actualizar este cliente.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCliente(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerUsuarioPorUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente cliente = clienteService.getClienteById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (usuario.getRol().equals("ADMIN") || cliente.getUsuario().getId().equals(usuario.getId())) {
            clienteService.eliminarCliente(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(403).body("No estás autorizado para eliminar este cliente.");
        }
    }
}
