package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.CarritoItem;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CarritoService {

    private final Map<String, Carrito> carritos = new HashMap<>();
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public CarritoService(ProductoRepository productoRepository, ClienteRepository clienteRepository,
            UsuarioService usuarioService) {
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioService = usuarioService;
    }

    // Método para obtener el clienteId a partir de la autenticación
    private String getClienteId(Authentication auth) {
        String username = auth.getName();
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para este usuario"));

        return cliente.getId();
    }

    // Método para obtener el carrito actual del cliente
    public Carrito getCarrito(Authentication auth) {
        String clienteId = getClienteId(auth);
        return carritos.computeIfAbsent(clienteId, Carrito::new);
    }

    // Método para agregar un producto al carrito
    public Carrito agregarProducto(Authentication auth, String productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock del producto: " + producto.getNombre());
        }

        Carrito carrito = getCarrito(auth);
        CarritoItem item = new CarritoItem(producto, cantidad);
        carrito.agregarItem(item);

        return carrito;
    }

    // Método para actualizar la cantidad de un producto en el carrito
    public Carrito actualizarCantidad(Authentication auth, String productoId, int cantidad) {
        // Verificar que el producto existe
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock del producto: " + producto.getNombre());
        }

        Carrito carrito = getCarrito(auth);
        carrito.actualizarItem(productoId, cantidad);

        return carrito;
    }

    // Método para eliminar un producto del carrito
    public Carrito eliminarProducto(Authentication auth, String productoId) {
        Carrito carrito = getCarrito(auth);
        carrito.eliminarItem(productoId);

        return carrito;
    }

    // Método para vaciar el carrito
    public void vaciarCarrito(Authentication auth) {
        Carrito carrito = getCarrito(auth);
        carrito.vaciar();
    }
}
