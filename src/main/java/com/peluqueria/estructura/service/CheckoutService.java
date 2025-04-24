package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.exception.ResourceNotFoundException;
import com.peluqueria.estructura.exception.StockInsuficienteException;
import com.peluqueria.estructura.exception.ValidationException;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckoutService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);

    private final CarritoService carritoService;
    private final FacturaService facturaService;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;

    @Autowired
    public CheckoutService(
            CarritoService carritoService,
            FacturaService facturaService,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ClienteService clienteService) {
        this.carritoService = carritoService;
        this.facturaService = facturaService;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteService = clienteService;
        logger.info("CheckoutService inicializado correctamente");
    }

    /**
     * Método para obtener el resumen de checkout
     */
    public CheckoutResumenDTO obtenerResumen(Authentication auth) {
        logger.info("Obteniendo resumen de checkout para usuario: {}", auth.getName());

        try {
            // Obtener el carrito del usuario
            Carrito carrito = carritoService.getCarrito(auth);
            logger.debug("Carrito obtenido para usuario {}: {} items", auth.getName(),
                    carrito.getItems() != null ? carrito.getItems().size() : 0);

            // Verificar que el carrito no esté vacío
            if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
                logger.warn("Intento de checkout con carrito vacío para usuario: {}", auth.getName());
                throw new ValidationException("El carrito está vacío");
            }

            // Buscar cliente por username
            Cliente cliente = encontrarClientePorUsername(auth.getName());
            if (cliente == null) {
                logger.error("Cliente no encontrado para el usuario: {}", auth.getName());
                throw new ResourceNotFoundException("Cliente", "usuario", auth.getName());
            }
            logger.debug("Cliente encontrado: {}, ID: {}", cliente.getNombre(), cliente.getId());

            // Crear el resumen de checkout
            CheckoutResumenDTO resumen = new CheckoutResumenDTO();
            resumen.setItems(carrito.getItems());
            resumen.setSubtotal(carrito.getTotal());
            resumen.setTotal(carrito.getTotal());
            resumen.setClienteNombre(cliente.getNombre());
            resumen.setClienteEmail(cliente.getEmail());

            // Verificar disponibilidad de stock para todos los productos
            boolean stockDisponible = true;
            StringBuilder mensajeError = new StringBuilder();

            for (var item : carrito.getItems()) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> {
                            logger.error("Producto no encontrado: {}", item.getProductoId());
                            return new ResourceNotFoundException("Producto", "id", item.getProductoId());
                        });

                if (producto.getStock() < item.getCantidad()) {
                    stockDisponible = false;
                    mensajeError.append("Stock insuficiente para el producto ")
                            .append(producto.getNombre())
                            .append(". Disponible: ")
                            .append(producto.getStock())
                            .append(", Solicitado: ")
                            .append(item.getCantidad())
                            .append(". ");

                    logger.warn("Stock insuficiente para producto: {}, disponible: {}, solicitado: {}",
                            producto.getNombre(), producto.getStock(), item.getCantidad());
                }
            }

            resumen.setStockDisponible(stockDisponible);
            logger.info("Resumen de checkout generado para {}: stock disponible: {}",
                    auth.getName(), stockDisponible);

            return resumen;

        } catch (ResourceNotFoundException | ValidationException e) {
            logger.error("Error específico al obtener resumen: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al obtener resumen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener el resumen de checkout: " + e.getMessage(), e);
        }
    }

    /**
     * Método para confirmar el checkout y generar la factura
     */
    @Transactional
    public Factura confirmarCheckout(Authentication auth, Map<String, String> checkoutInfo) {
        logger.info("Confirmando checkout para usuario: {}", auth.getName());

        try {
            // Obtener el carrito
            Carrito carrito = carritoService.getCarrito(auth);

            // Verificar que el carrito no esté vacío
            if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
                logger.warn("Intento de confirmar checkout con carrito vacío para usuario: {}", auth.getName());
                throw new ValidationException("El carrito está vacío");
            }

            // Buscar cliente por username
            Cliente cliente = encontrarClientePorUsername(auth.getName());
            if (cliente == null) {
                logger.error("Cliente no encontrado para el usuario: {}", auth.getName());
                throw new ResourceNotFoundException("Cliente", "usuario", auth.getName());
            }
            logger.debug("Cliente encontrado: {}, ID: {}", cliente.getNombre(), cliente.getId());

            // Verificar stock suficiente para todos los productos
            for (var item : carrito.getItems()) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> {
                            logger.error("Producto no encontrado: {}", item.getProductoId());
                            return new ResourceNotFoundException("Producto", "id", item.getProductoId());
                        });

                if (producto.getStock() < item.getCantidad()) {
                    logger.warn("Stock insuficiente para producto: {}, disponible: {}, solicitado: {}",
                            producto.getNombre(), producto.getStock(), item.getCantidad());
                    throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), item.getCantidad());
                }
            }

            // Convertir los items del carrito a detalles de factura
            List<DetalleFactura> detalles = new ArrayList<>();
            for (var item : carrito.getItems()) {
                DetalleFactura detalle = new DetalleFactura();
                detalle.setId(UUID.randomUUID().toString());
                detalle.setProductoId(item.getProductoId());
                detalle.setProductoNombre(item.getNombre());
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(item.getPrecioUnitario());
                detalle.setSubtotal(item.getSubtotal());

                detalles.add(detalle);
                logger.debug("Detalle creado para producto: {}, cantidad: {}", item.getNombre(), item.getCantidad());
            }

            // Crear la factura
            Factura factura = new Factura();
            factura.setId(UUID.randomUUID().toString());
            factura.setCliente(cliente);
            factura.setFecha(LocalDateTime.now());
            factura.setEstado("PENDIENTE");
            factura.setDetalles(detalles);

            // Calcular totales
            factura.setTotal(carrito.getTotal());

            // Guardar la factura
            Factura facturaGuardada = facturaService.save(factura);
            logger.info("Factura creada exitosamente: ID={}, Total={}, Cliente={}",
                    facturaGuardada.getId(), facturaGuardada.getTotal(), cliente.getNombre());

            // Actualizar el inventario
            actualizarInventario(detalles);

            // Vaciar el carrito
            carritoService.vaciarCarrito(auth);
            logger.debug("Carrito vaciado para el usuario: {}", auth.getName());

            logger.info("Checkout completado exitosamente para el usuario: {}", auth.getName());
            return facturaGuardada;

        } catch (ResourceNotFoundException | ValidationException | StockInsuficienteException e) {
            logger.error("Error específico al confirmar checkout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al confirmar checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al confirmar checkout: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el inventario basado en los detalles de la factura
     */
    private void actualizarInventario(List<DetalleFactura> detalles) {
        logger.debug("Actualizando inventario para {} detalles", detalles.size());

        for (DetalleFactura detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                    .orElseThrow(() -> {
                        logger.error("Producto no encontrado al actualizar inventario: {}", detalle.getProductoId());
                        return new ResourceNotFoundException("Producto", "id", detalle.getProductoId());
                    });

            int nuevoStock = producto.getStock() - detalle.getCantidad();
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
            logger.debug("Stock actualizado para producto {}: {} -> {}",
                    producto.getNombre(), producto.getStock() + detalle.getCantidad(), nuevoStock);
        }
    }

    /**
     * Método auxiliar para encontrar un cliente por username
     */
    private Cliente encontrarClientePorUsername(String username) {
        logger.debug("Buscando cliente para el usuario: {}", username);

        try {
            // Buscar usuario primero
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseGet(() -> {
                        // Si no se encuentra con findByUsername, intentar con findByEmail
                        logger.debug("Usuario no encontrado con username, intentando con email: {}", username);
                        return usuarioRepository.findByEmail(username)
                                .orElseThrow(() -> {
                                    logger.error("Usuario no encontrado ni por username ni por email: {}", username);
                                    return new ResourceNotFoundException("Usuario", "username/email", username);
                                });
                    });

            logger.debug("Usuario encontrado: ID={}, Username={}, Email={}",
                    usuario.getId(), usuario.getUsername(), usuario.getEmail());

            // Buscar el cliente asociado al usuario
            return clienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> {
                        logger.error("Cliente no encontrado para el usuario con ID: {}", usuario.getId());
                        return new ResourceNotFoundException("Cliente", "usuario.id", usuario.getId());
                    });
        } catch (Exception e) {
            logger.error("Error al buscar cliente por username: {}", e.getMessage(), e);
            throw e;
        }
    }
}
