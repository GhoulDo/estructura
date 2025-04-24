package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.CheckoutResumenDTO;
import com.peluqueria.estructura.entity.Carrito;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@Service
public class CheckoutService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);
    private final CarritoService carritoService;
    private final FacturaService facturaService;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioFacturaService inventarioFacturaService;
    private final CalculadoraFacturaService calculadoraFacturaService;

    @Autowired
    public CheckoutService(
            CarritoService carritoService,
            FacturaService facturaService,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            InventarioFacturaService inventarioFacturaService,
            CalculadoraFacturaService calculadoraFacturaService) {
        this.carritoService = carritoService;
        this.facturaService = facturaService;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioFacturaService = inventarioFacturaService;
        this.calculadoraFacturaService = calculadoraFacturaService;
    }

    /**
     * Método para obtener el resumen de checkout
     */
    public CheckoutResumenDTO obtenerResumen(Authentication auth) {
        logger.info("Obteniendo resumen de checkout para usuario: {}", auth.getName());

        try {
            // Obtener el carrito del usuario
            Carrito carrito = carritoService.getCarrito(auth);

            // Verificar que el carrito no esté vacío
            if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
                logger.warn("Intento de checkout con carrito vacío");
                throw new RuntimeException("El carrito está vacío");
            }

            // Buscar el cliente asociado al usuario
            String username = auth.getName();

            // Intentamos obtener el usuario directamente por username
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (!usuarioOpt.isPresent()) {
                // Intentamos con findByUsernameIgnoreCase
                usuarioOpt = usuarioRepository.findByUsernameIgnoreCase(username);
            }

            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                throw new RuntimeException("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario encontrado: {}, ID: {}", usuario.getUsername(), usuario.getId());

            // Buscar el cliente por usuarioId
            Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario ID: {}", usuario.getId());
                throw new RuntimeException("Cliente no encontrado para este usuario");
            }

            Cliente cliente = clienteOpt.get();
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
            for (var item : carrito.getItems()) {
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

                if (producto.getStock() < item.getCantidad()) {
                    logger.warn("Stock insuficiente para el producto {}: disponible={}, solicitado={}",
                            producto.getNombre(), producto.getStock(), item.getCantidad());
                    stockDisponible = false;
                    break;
                }
            }

            resumen.setStockDisponible(stockDisponible);
            logger.info("Resumen de checkout generado con éxito para {}", username);

            return resumen;

        } catch (Exception e) {
            logger.error("Error al obtener resumen de checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener resumen de checkout: " + e.getMessage(), e);
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
                logger.warn("Intento de confirmar checkout con carrito vacío");
                throw new RuntimeException("El carrito está vacío");
            }

            // Obtener el cliente asociado al usuario
            String username = auth.getName();

            // Intentamos obtener el usuario directamente por username
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (!usuarioOpt.isPresent()) {
                // Intentamos con findByUsernameIgnoreCase
                usuarioOpt = usuarioRepository.findByUsernameIgnoreCase(username);
            }

            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                throw new RuntimeException("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            logger.debug("Usuario encontrado: {}, ID: {}", usuario.getUsername(), usuario.getId());

            // Buscar el cliente por usuarioId
            Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioId(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario ID: {}", usuario.getId());
                throw new RuntimeException("Cliente no encontrado para este usuario");
            }

            Cliente cliente = clienteOpt.get();
            logger.debug("Cliente encontrado: {}, ID: {}", cliente.getNombre(), cliente.getId());

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
            }

            // Verificar stock suficiente
            inventarioFacturaService.verificarStockSuficiente(detalles);

            // Crear la factura
            Factura factura = new Factura();
            factura.setId(UUID.randomUUID().toString());
            factura.setCliente(cliente);
            factura.setFecha(LocalDateTime.now());
            factura.setEstado("PENDIENTE");
            factura.setDetalles(detalles);

            // Calcular totales
            calculadoraFacturaService.recalcularFactura(factura);

            // Guardar la factura
            Factura facturaGuardada = facturaService.save(factura);

            // Actualizar el inventario
            inventarioFacturaService.actualizarInventario(detalles);

            // Vaciar el carrito
            carritoService.vaciarCarrito(auth);

            logger.info("Checkout confirmado y factura generada con ID: {} para usuario: {}",
                    facturaGuardada.getId(), username);

            return facturaGuardada;

        } catch (Exception e) {
            logger.error("Error al confirmar checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al confirmar checkout: " + e.getMessage(), e);
        }
    }
}
