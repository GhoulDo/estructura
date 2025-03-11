// src/main/java/com/peluqueria/estructura/service/DetalleFacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.DetalleFacturaDTO;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.DetalleFactura;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Producto;
import com.peluqueria.estructura.entity.Servicio;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.DetalleFacturaRepository;
import com.peluqueria.estructura.repository.FacturaRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import com.peluqueria.estructura.repository.ServicioRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturaRepository facturaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final FacturaService facturaService;

    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository, 
                               FacturaRepository facturaRepository,
                               ProductoRepository productoRepository,
                               ServicioRepository servicioRepository,
                               ClienteRepository clienteRepository,
                               UsuarioRepository usuarioRepository,
                               FacturaService facturaService) {
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturaRepository = facturaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.facturaService = facturaService;
    }

    // Método para verificar si el usuario tiene acceso a la factura
    private boolean verificarAccesoFactura(Long facturaId, Authentication auth) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        // Si es ADMIN, tiene acceso
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }
        
        // Si es CLIENTE, verificar que la factura le pertenezca
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        return factura.getCliente().getId().equals(cliente.getId());
    }

    public List<DetalleFacturaDTO> getAllDetalles(Long facturaId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para acceder a esta factura");
        }
        
        return detalleFacturaRepository.findByFacturaId(facturaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DetalleFacturaDTO getDetalleById(Long facturaId, Long detalleId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para acceder a esta factura");
        }
        
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        return convertToDTO(detalle);
    }

    @Transactional
    public DetalleFacturaDTO createDetalle(Long facturaId, DetalleFacturaDTO detalleDTO, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        DetalleFactura detalle = new DetalleFactura();
        detalle.setFactura(factura);
        
        // Verificar si se está agregando un producto o un servicio
        if (detalleDTO.getProductoId() != null) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            detalle.setProducto(producto);
            
            // Calcular subtotal basado en el precio del producto y la cantidad
            BigDecimal precioProducto = producto.getPrecio();
            detalle.setSubtotal(precioProducto.multiply(new BigDecimal(detalleDTO.getCantidad())));
        } else if (detalleDTO.getServicioId() != null) {
            Servicio servicio = servicioRepository.findById(detalleDTO.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            detalle.setServicio(servicio);
            
            // Calcular subtotal basado en el precio del servicio
            detalle.setSubtotal(servicio.getPrecio());
        } else {
            throw new RuntimeException("Debe especificar un producto o un servicio");
        }
        
        detalle.setCantidad(detalleDTO.getCantidad());
        
        // Guardar el detalle
        DetalleFactura savedDetalle = detalleFacturaRepository.save(detalle);
        
        // Recalcular el total de la factura
        facturaService.calcularTotalFactura(facturaId, auth);
        
        return convertToDTO(savedDetalle);
    }

    @Transactional
    public DetalleFacturaDTO updateDetalle(Long facturaId, Long detalleId, DetalleFacturaDTO detalleDTO, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        // Actualizar el producto si se proporciona
        if (detalleDTO.getProductoId() != null) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            detalle.setProducto(producto);
            
            // Recalcular subtotal
            BigDecimal precioProducto = producto.getPrecio();
            detalle.setSubtotal(precioProducto.multiply(new BigDecimal(detalleDTO.getCantidad())));
        }
        
        // Actualizar el servicio si se proporciona
        if (detalleDTO.getServicioId() != null) {
            Servicio servicio = servicioRepository.findById(detalleDTO.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            detalle.setServicio(servicio);
            
            // Recalcular subtotal para servicio
            detalle.setSubtotal(servicio.getPrecio());
        }
        
        // Actualizar cantidad
        detalle.setCantidad(detalleDTO.getCantidad());
        
        // Guardar cambios
        DetalleFactura updatedDetalle = detalleFacturaRepository.save(detalle);
        
        // Recalcular el total de la factura
        facturaService.calcularTotalFactura(facturaId, auth);
        
        return convertToDTO(updatedDetalle);
    }

    @Transactional
    public void deleteDetalle(Long facturaId, Long detalleId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        DetalleFactura detalle = detalleFacturaRepository.findByIdAndFacturaId(detalleId, facturaId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        detalleFacturaRepository.delete(detalle);
        
        // Recalcular el total de la factura
        facturaService.calcularTotalFactura(facturaId, auth);
    }

    private DetalleFacturaDTO convertToDTO(DetalleFactura detalle) {
        DetalleFacturaDTO detalleDTO = new DetalleFacturaDTO();
        detalleDTO.setId(detalle.getId());
        
        if (detalle.getProducto() != null) {
            detalleDTO.setProductoId(detalle.getProducto().getId());
        }
        
        if (detalle.getServicio() != null) {
            detalleDTO.setServicioId(detalle.getServicio().getId());
        }
        
        detalleDTO.setCantidad(detalle.getCantidad());
        detalleDTO.setSubtotal(detalle.getSubtotal());
        
        return detalleDTO;
    }
}