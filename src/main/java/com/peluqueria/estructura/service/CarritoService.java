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
import java.util.Optional;

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
     * Método mejorado para obtener el clienteId a partir de la autenticación
     */
    private String getClienteId(Authentication auth) {
        if (auth == null) {
            logger.error("Error: Authentication es null");
            throw new RuntimeException("No hay autenticación");
        }

        String username = auth.getName();
        logger.info("Buscando cliente para el usuario autenticado: {}", username);

        try {
            // Método 1: Buscar directamente por username (case insensitive)
            logger.debug("Método 1: Buscar usuario por username exacto");
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameIgnoreCase(username);

            if (!usuarioOpt.isPresent()) {
                logger.debug("No se encontró usuario con username exacto: {}. Probando búsqueda por email", username);
                // Muchas implementaciones almacenan emails como usernames, intentamos con
                // findByEmail
                usuarioOpt = usuarioRepository.findByEmail(username);
            }

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                logger.info("Usuario encontrado por username/email: {} con ID: {}", usuario.getUsername(),
                        usuario.getId());

                // Buscar cliente por usuarioId
                Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    logger.info("Cliente encontrado: {} con ID: {}", cliente.getNombre(), cliente.getId());
                    return cliente.getId();
                } else {
                    logger.warn("No se encontró cliente para el usuario con ID: {}", usuario.getId());
                }
            }

            // Método 2: Buscar entre todos los clientes por username del usuario
            logger.debug("Método 2: Buscar entre todos los clientes");
            List<Cliente> clientes = clienteRepository.findAll();
            logger.debug("Total de clientes en la base de datos: {}", clientes.size());

            for (Cliente cliente : clientes) {
                if (cliente.getUsuario() != null) {
                    logger.debug("Cliente ID: {}, Nombre: {}, Usuario: {}",
                            cliente.getId(), cliente.getNombre(),
                            cliente.getUsuario() != null ? cliente.getUsuario().getUsername() : "null");

                    if (username.equalsIgnoreCase(cliente.getUsuario().getUsername()) ||
                            (cliente.getEmail() != null && username.equalsIgnoreCase(cliente.getEmail()))) {
                        logger.info("Cliente encontrado por búsqueda manual: {} con ID: {}",
                                cliente.getNombre(), cliente.getId());
                        return cliente.getId();
                    }
                }
            }

            // Método 3: Consulta directa a la base de datos
            logger.debug("Método 3: Consulta customizada");
            List<Cliente> clientesPorUsername = clienteRepository.findByUsuarioUsername(username);
            if (!clientesPorUsername.isEmpty()) {
                Cliente cliente = clientesPorUsername.get(0);
                logger.info("Cliente encontrado por consulta customizada: {} con ID: {}",
                        cliente.getNombre(), cliente.getId());
                return cliente.getId();
            }

            // Si llegamos aquí, no se encontró el cliente
            logger.error("No se encontró cliente para el usuario: {} después de intentar todos los métodos", username);
            throw new RuntimeException("Cliente no encontrado para el usuario: " + username);

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
