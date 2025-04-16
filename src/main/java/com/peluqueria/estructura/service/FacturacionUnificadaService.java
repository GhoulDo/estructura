package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.repository.CitaRepository;
import com.peluqueria.estructura.repository.FacturaRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import com.peluqueria.estructura.repository.ServicioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacturacionUnificadaService {

    private final FacturaRepository facturaRepository;
    private final CitaRepository citaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final ProductoService productoService;

    @Autowired
    public FacturacionUnificadaService(
            FacturaRepository facturaRepository,
            CitaRepository citaRepository,
            ProductoRepository productoRepository,
            ServicioRepository servicioRepository,
            ProductoService productoService) {
        this.facturaRepository = facturaRepository;
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.productoService = productoService;
    }

    /**
     * Crea una factura a partir de una cita, permitiendo opcionalmente añadir productos.
     * @param citaId ID de la cita a facturar
     * @param productosIds Lista opcional de IDs de productos a incluir en la factura
     * @param cantidades Lista de cantidades correspondientes a cada producto
     * @return La factura creada
     */
    public Factura facturarCitaConProductos(String citaId, List<String> productosIds, List<Integer> cantidades) {
        // Buscar la cita
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
        
        // Verificar que la cita no haya sido facturada ya
        if (cita.isFacturada()) {
            throw new RuntimeException("La cita ya ha sido facturada");
        }
        
        // Crear la factura
        Factura factura = new Factura();
        factura.setId(UUID.randomUUID().toString());
        factura.setCliente(cita.getMascota().getCliente());
        factura.setFecha(LocalDateTime.now());
        factura.setEstado("PENDIENTE");
        factura.setDetalles(new ArrayList<>());
        
        // Añadir el servicio de la cita como detalle
        DetalleFactura detalleServicio = new DetalleFactura();
        detalleServicio.setId(UUID.randomUUID().toString());
        detalleServicio.setServicioId(cita.getServicio().getId());
        detalleServicio.setServicioNombre(cita.getServicio().getNombre());
        detalleServicio.setCantidad(1);
        detalleServicio.setPrecioUnitario(cita.getServicio().getPrecio());
        detalleServicio.setSubtotal(cita.getServicio().getPrecio());
        factura.getDetalles().add(detalleServicio);
        
        // Añadir productos si se han especificado
        if (productosIds != null && !productosIds.isEmpty()) {
            if (cantidades == null || productosIds.size() != cantidades.size()) {
                throw new RuntimeException("La lista de cantidades debe tener el mismo tamaño que la lista de productos");
            }
            
            for (int i = 0; i < productosIds.size(); i++) {
                String productoId = productosIds.get(i);
                int cantidad = cantidades.get(i);
                
                Producto producto = productoRepository.findById(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
                
                // Verificar stock suficiente
                if (producto.getStock() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                
                // Reducir stock
                productoService.actualizarStock(productoId, -cantidad);
                
                // Añadir detalle de producto
                DetalleFactura detalleProducto = new DetalleFactura();
                detalleProducto.setId(UUID.randomUUID().toString());
                detalleProducto.setProductoId(productoId);
                detalleProducto.setProductoNombre(producto.getNombre());
                detalleProducto.setCantidad(cantidad);
                detalleProducto.setPrecioUnitario(producto.getPrecio());
                detalleProducto.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
                factura.getDetalles().add(detalleProducto);
            }
        }
        
        // Calcular el total de la factura
        BigDecimal total = factura.getDetalles().stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        factura.setTotal(total);
        
        // Guardar la factura
        factura = facturaRepository.save(factura);
        
        // Marcar la cita como facturada y actualizar su referencia a la factura
        cita.setFacturada(true);
        cita.setFacturaId(factura.getId());
        citaRepository.save(cita);
        
        return factura;
    }
    
    /**
     * Añade productos a una factura existente
     * @param facturaId ID de la factura
     * @param productosIds IDs de los productos a añadir
     * @param cantidades Cantidades de cada producto
     * @return La factura actualizada
     */
    public Factura agregarProductosAFactura(String facturaId, List<String> productosIds, List<Integer> cantidades) {
        // Buscar la factura
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));
        
        // Verificar que la factura esté en estado PENDIENTE
        if (!"PENDIENTE".equals(factura.getEstado())) {
            throw new RuntimeException("Solo se pueden agregar productos a facturas en estado PENDIENTE");
        }
        
        // Verificar parámetros
        if (productosIds == null || productosIds.isEmpty()) {
            throw new RuntimeException("Debe especificar al menos un producto");
        }
        
        if (cantidades == null || productosIds.size() != cantidades.size()) {
            throw new RuntimeException("La lista de cantidades debe tener el mismo tamaño que la lista de productos");
        }
        
        // Inicializar la lista de detalles si es null
        if (factura.getDetalles() == null) {
            factura.setDetalles(new ArrayList<>());
        }
        
        // Añadir los productos
        for (int i = 0; i < productosIds.size(); i++) {
            String productoId = productosIds.get(i);
            int cantidad = cantidades.get(i);
            
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
            
            // Verificar stock suficiente
            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            
            // Reducir stock
            productoService.actualizarStock(productoId, -cantidad);
            
            // Añadir detalle de producto
            DetalleFactura detalleProducto = new DetalleFactura();
            detalleProducto.setId(UUID.randomUUID().toString());
            detalleProducto.setProductoId(productoId);
            detalleProducto.setProductoNombre(producto.getNombre());
            detalleProducto.setCantidad(cantidad);
            detalleProducto.setPrecioUnitario(producto.getPrecio());
            detalleProducto.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
            factura.getDetalles().add(detalleProducto);
        }
        
        // Recalcular el total de la factura
        BigDecimal total = factura.getDetalles().stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        factura.setTotal(total);
        
        // Guardar la factura actualizada
        return facturaRepository.save(factura);
    }
    
    /**
     * Cambia el estado de una factura a PAGADA
     * @param facturaId ID de la factura a marcar como pagada
     * @return La factura actualizada
     */
    public Factura pagarFactura(String facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));
        
        factura.setEstado("PAGADA");
        return facturaRepository.save(factura);
    }
    
    /**
     * Obtiene todas las facturas de un cliente
     * @param clienteId ID del cliente
     * @return Lista de facturas del cliente
     */
    public List<Factura> obtenerFacturasCliente(String clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }
}