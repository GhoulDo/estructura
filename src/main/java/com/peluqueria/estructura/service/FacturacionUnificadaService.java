package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Cita;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FacturacionUnificadaService {

    private final FacturaService facturaService;
    private final CitaFacturaService citaFacturaService;
    private final InventarioFacturaService inventarioFacturaService;
    private final CalculadoraFacturaService calculadoraFacturaService;
    private final ProductoRepository productoRepository;

    @Autowired
    public FacturacionUnificadaService(
            FacturaService facturaService,
            CitaFacturaService citaFacturaService,
            InventarioFacturaService inventarioFacturaService,
            CalculadoraFacturaService calculadoraFacturaService,
            ProductoRepository productoRepository) {
        this.facturaService = facturaService;
        this.citaFacturaService = citaFacturaService;
        this.inventarioFacturaService = inventarioFacturaService;
        this.calculadoraFacturaService = calculadoraFacturaService;
        this.productoRepository = productoRepository;
    }

    /**
     * Crea una factura a partir de una cita, permitiendo opcionalmente añadir productos.
     */
    @Transactional
    public Factura facturarCitaConProductos(String citaId, List<String> productosIds, List<Integer> cantidades) {
        // Obtener la cita y validar
        Cita cita = citaFacturaService.obtenerCitaParaFacturar(citaId);
        
        // Crear la factura
        Factura factura = new Factura();
        factura.setId(UUID.randomUUID().toString());
        factura.setCliente(cita.getMascota().getCliente());
        factura.setEstado("PENDIENTE");
        factura.setDetalles(new ArrayList<>());
        
        // Añadir el servicio de la cita como detalle
        DetalleFactura detalleServicio = citaFacturaService.crearDetalleServicioDesdeCita(cita);
        factura.getDetalles().add(detalleServicio);
        
        // Añadir productos si se han especificado
        if (productosIds != null && !productosIds.isEmpty()) {
            if (cantidades == null || productosIds.size() != cantidades.size()) {
                throw new RuntimeException("La lista de cantidades debe tener el mismo tamaño que la lista de productos");
            }
            
            // Crear detalles de productos
            List<DetalleFactura> detallesProductos = crearDetallesProductos(productosIds, cantidades);
            
            // Verificar stock
            inventarioFacturaService.verificarStockSuficiente(detallesProductos);
            
            // Añadir detalles a la factura
            factura.getDetalles().addAll(detallesProductos);
        }
        
        // Calcular totales
        calculadoraFacturaService.recalcularFactura(factura);
        
        // Guardar la factura
        factura = facturaService.save(factura);
        
        // Actualizar inventario
        inventarioFacturaService.actualizarInventario(
            factura.getDetalles().stream()
                .filter(d -> d.getProductoId() != null)
                .toList()
        );
        
        // Marcar la cita como facturada
        citaFacturaService.marcarCitaFacturada(citaId, factura.getId());
        
        return factura;
    }
    
    /**
     * Añade productos a una factura existente
     */
    @Transactional
    public Factura agregarProductosAFactura(String facturaId, List<String> productosIds, List<Integer> cantidades) {
        // Buscar la factura
        Factura factura = facturaService.findById(facturaId)
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
        
        // Crear detalles de productos
        List<DetalleFactura> nuevosDetalles = crearDetallesProductos(productosIds, cantidades);
        
        // Verificar stock
        inventarioFacturaService.verificarStockSuficiente(nuevosDetalles);
        
        // Añadir los nuevos detalles
        factura.getDetalles().addAll(nuevosDetalles);
        
        // Recalcular el total de la factura
        calculadoraFacturaService.recalcularFactura(factura);
        
        // Actualizar inventario
        inventarioFacturaService.actualizarInventario(nuevosDetalles);
        
        // Guardar la factura actualizada
        return facturaService.save(factura);
    }
    
    /**
     * Cambia el estado de una factura a PAGADA
     */
    public Factura pagarFactura(String facturaId) {
        return facturaService.actualizarEstadoFactura(facturaId, "PAGADA");
    }
    
    /**
     * Obtiene todas las facturas de un cliente
     */
    public List<Factura> obtenerFacturasCliente(String clienteId) {
        return facturaService.findByClienteId(clienteId);
    }
    
    /**
     * Método auxiliar para crear detalles de productos
     */
    private List<DetalleFactura> crearDetallesProductos(List<String> productosIds, List<Integer> cantidades) {
        List<DetalleFactura> detalles = new ArrayList<>();
        
        for (int i = 0; i < productosIds.size(); i++) {
            String productoId = productosIds.get(i);
            int cantidad = cantidades.get(i);
            
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
            
            DetalleFactura detalleProducto = new DetalleFactura();
            detalleProducto.setId(UUID.randomUUID().toString());
            detalleProducto.setProductoId(productoId);
            detalleProducto.setProductoNombre(producto.getNombre());
            detalleProducto.setCantidad(cantidad);
            detalleProducto.setPrecioUnitario(producto.getPrecio());
            detalleProducto.setSubtotal(producto.getPrecio().multiply(new java.math.BigDecimal(cantidad)));
            
            detalles.add(detalleProducto);
        }
        
        return detalles;
    }
}