package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.CarritoItem;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarritoService {

    private static final Logger logger = LoggerFactory.getLogger(CarritoService.class);
    private final Map<String, Carrito> carritos = new HashMap<>();
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CarritoService(ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Método para obtener el clienteId a partir de la autenticación
     */
    private String getClienteId(Authentication auth) {
        if (auth == null) {
            logger.error("Error: Authentication es null");
            throw new RuntimeException("No hay autenticación");
        }

        String username = auth.getName();
        logger.debug("Obteniendo cliente para el usuario: {}", username);

        try {
            // Intento buscar directamente el cliente por username sin pasar por Usuario
            List<Cliente> clientes = clienteRepository.findAll();

            for (Cliente cliente : clientes) {
                if (cliente.getUsuario() != null && username.equalsIgnoreCase(cliente.getUsuario().getUsername())) {
                    logger.info("Cliente encontrado directamente por username: {} con ID: {}",
                            cliente.getNombre(), cliente.getId());
                    return cliente.getId();
                }
            }

            // Si no se encontró buscando directamente, intentamos el flujo normal
            Usuario usuario = null;

            // Obtener todos los usuarios para diagnosticar
            List<Usuario> usuarios = usuarioRepository.findAll();
            logger.debug("Total de usuarios en la base de datos: {}", usuarios.size());

            for (Usuario u : usuarios) {
                logger.debug("Usuario en DB: id={}, username={}", u.getId(), u.getUsername());
                if (username.equalsIgnoreCase(u.getUsername())) {
                    usuario = u;
                    break;
                }
            }

            if (usuario == null) {
                logger.error("Usuario no encontrado después de revisar todos los usuarios: {}", username);
                throw new RuntimeException(
                        "Usuario no encontrado después de buscar en toda la base de datos: " + username);
            }

            logger.info("Usuario encontrado manualmente: ID={}, username={}", usuario.getId(), usuario.getUsername());

            // Buscar cliente asociado al usuario encontrado
            Cliente clienteEncontrado = null;
            for (Cliente c : clientes) {
                if (c.getUsuario() != null && c.getUsuario().getId().equals(usuario.getId())) {
                    clienteEncontrado = c;
                    break;
                }
            }

            if (clienteEncontrado == null) {
                logger.error("Cliente no encontrado para el usuario con ID: {}", usuario.getId());
                throw new RuntimeException("Cliente no encontrado para el usuario: " + username);
            }

            logger.info("Cliente encontrado: {} con ID: {}", clienteEncontrado.getNombre(), clienteEncontrado.getId());
            return clienteEncontrado.getId();

        } catch (Exception e) {
            logger.error("Error al buscar cliente para el usuario {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error al buscar cliente: " + e.getMessage());
        }
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
