package com.peluqueria.estructura.controller;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // Endpoint para ver el carrito actual
    @GetMapping
    public ResponseEntity<Carrito> getCarrito(Authentication auth) {
        return ResponseEntity.ok(carritoService.getCarrito(auth));
    }

    // Endpoint para agregar un producto al carrito
    @PostMapping("/agregar")
    public ResponseEntity<Carrito> agregarProducto(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String productoId = (String) request.get("productoId");
        int cantidad = Integer.parseInt(request.get("cantidad").toString());

        return ResponseEntity.ok(carritoService.agregarProducto(auth, productoId, cantidad));
    }

    // Endpoint para actualizar la cantidad de un producto
    @PutMapping("/actualizar")
    public ResponseEntity<Carrito> actualizarCantidad(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String productoId = (String) request.get("productoId");
        int cantidad = Integer.parseInt(request.get("cantidad").toString());

        return ResponseEntity.ok(carritoService.actualizarCantidad(auth, productoId, cantidad));
    }

    // Endpoint para eliminar un producto del carrito
    @DeleteMapping("/eliminar/{productoId}")
    public ResponseEntity<Carrito> eliminarProducto(
            Authentication auth,
            @PathVariable String productoId) {

        return ResponseEntity.ok(carritoService.eliminarProducto(auth, productoId));
    }

    // Endpoint para vaciar el carrito
    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(Authentication auth) {
        carritoService.vaciarCarrito(auth);
        return ResponseEntity.noContent().build();
    }
}
