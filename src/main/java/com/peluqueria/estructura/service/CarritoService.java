package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.CarritoItem;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CarritoService {

    private static final Logger logger = LoggerFactory.getLogger(CarritoService.class);
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

    /**
     * Método para obtener el clienteId a partir de la autenticación
     */
    private String getClienteId(Authentication auth) {
        String username = auth.getName();
        logger.debug("Obteniendo cliente para el usuario: {}", username);

        Optional<Usuario> usuario = usuarioService.findByUsername(username);
        if (!usuario.isPresent()) {
            logger.error("Usuario no encontrado: {}", username);
            throw new RuntimeException("Usuario no encontrado");
        }

        logger.debug("Usuario encontrado con ID: {}", usuario.get().getId());

        Optional<Cliente> cliente = clienteRepository.findByUsuarioId(usuario.get().getId());
        if (!cliente.isPresent()) {
            logger.error("Cliente no encontrado para el usuario: {}", username);
            throw new RuntimeException("Cliente no encontrado para este usuario");
        }

        logger.debug("Cliente encontrado con ID: {}", cliente.get().getId());
        return cliente.get().getId();
    }

    /**
     * Método para obtener el carrito actual del cliente
     */
    public Carrito getCarrito(Authentication auth) {
        String clienteId = getClienteId(auth);
        logger.debug("Obteniendo carrito para el cliente: {}", clienteId);
        return carritos.computeIfAbsent(clienteId, Carrito::new);
    }

    /**
     * Método para agregar un producto al carrito
     */
    public Carrito agregarProducto(Authentication auth, String productoId, int cantidad) {
        logger.debug("Agregando producto al carrito - productoId: {}, cantidad: {}", productoId, cantidad);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock del producto: " + producto.getNombre());
        }

        Carrito carrito = getCarrito(auth);
        CarritoItem item = new CarritoItem(producto, cantidad);
        carrito.agregarItem(item);

        logger.debug("Producto agregado al carrito exitosamente");
        return carrito;
    }

    /**
     * Método para actualizar la cantidad de un producto en el carrito
     */
    public Carrito actualizarCantidad(Authentication auth, String productoId, int cantidad) {
        logger.debug("Actualizando cantidad en el carrito - productoId: {}, cantidad: {}", productoId, cantidad);

        // Verificar que el producto existe
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock del producto: " + producto.getNombre());
        }

        Carrito carrito = getCarrito(auth);
        carrito.actualizarItem(productoId, cantidad);

        logger.debug("Cantidad actualizada exitosamente");
        return carrito;
    }

    /**
     * Método para eliminar un producto del carrito
     */
    public Carrito eliminarProducto(Authentication auth, String productoId) {
        logger.debug("Eliminando producto del carrito - productoId: {}", productoId);

        Carrito carrito = getCarrito(auth);
        carrito.eliminarItem(productoId);

        logger.debug("Producto eliminado exitosamente");
        return carrito;
    }

    /**
     * Método para vaciar el carrito
     */
    public void vaciarCarrito(Authentication auth) {
        logger.debug("Vaciando carrito");

        Carrito carrito = getCarrito(auth);
        carrito.vaciar();

        logger.debug("Carrito vaciado exitosamente");
    }
}
