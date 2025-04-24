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

    private final CarritoService carritoService;
    private final FacturaService facturaService;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;
    private final InventarioFacturaService inventarioFacturaService;
    private final CalculadoraFacturaService calculadoraFacturaService;

    @Autowired
    public CheckoutService(
            CarritoService carritoService,
            FacturaService facturaService,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            UsuarioService usuarioService,
            InventarioFacturaService inventarioFacturaService,
            CalculadoraFacturaService calculadoraFacturaService) {
        this.carritoService = carritoService;
        this.facturaService = facturaService;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioService = usuarioService;
        this.inventarioFacturaService = inventarioFacturaService;
        this.calculadoraFacturaService = calculadoraFacturaService;
    }

    // Método para obtener el resumen de checkout
    public CheckoutResumenDTO obtenerResumen(Authentication auth) {
        Carrito carrito = carritoService.getCarrito(auth);
        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        String username = auth.getName();
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        CheckoutResumenDTO resumen = new CheckoutResumenDTO();
        resumen.setItems(carrito.getItems());
        resumen.setSubtotal(carrito.getTotal());
        resumen.setTotal(carrito.getTotal());
        resumen.setClienteNombre(cliente.getNombre());
        resumen.setClienteEmail(cliente.getEmail());

        // Verificar disponibilidad de stock
        boolean stockDisponible = true;
        for (var item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                stockDisponible = false;
                break;
            }
        }

        resumen.setStockDisponible(stockDisponible);

        return resumen;
    }

    // Método para confirmar el checkout y generar la factura
    @Transactional
    public Factura confirmarCheckout(Authentication auth, Map<String, String> checkoutInfo) {
        Carrito carrito = carritoService.getCarrito(auth);
        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Obtener el cliente
        String username = auth.getName();
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Convertir items del carrito a detalles de factura
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

        // Verificar disponibilidad de stock
        inventarioFacturaService.verificarStockSuficiente(detalles);

        // Crear la factura
        Factura factura = new Factura();
        factura.setId(UUID.randomUUID().toString());
        factura.setCliente(cliente);
        factura.setFecha(LocalDateTime.now());
        factura.setEstado("PENDIENTE");
        factura.setDetalles(detalles);

        // Añadir información adicional si se proporciona
        String direccionEntrega = checkoutInfo.get("direccionEntrega");
        if (direccionEntrega != null && !direccionEntrega.isEmpty()) {
            // Aquí podrías guardar la dirección de entrega en un campo adicional de la
            // factura
            // Por ahora lo dejamos comentado ya que el modelo de Factura no tiene ese campo
            // factura.setDireccionEntrega(direccionEntrega);
        }

        // Calcular el total
        calculadoraFacturaService.recalcularFactura(factura);

        // Guardar la factura
        Factura facturaGuardada = facturaService.save(factura);

        // Actualizar el inventario
        inventarioFacturaService.actualizarInventario(detalles);

        // Vaciar el carrito
        carritoService.vaciarCarrito(auth);

        return facturaGuardada;
    }
}
