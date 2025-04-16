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
import com.peluqueria.estructura.repository.FacturaRepository;
import com.peluqueria.estructura.repository.ProductoRepository;
import com.peluqueria.estructura.repository.ServicioRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DetalleFacturaService {

    private final FacturaRepository facturaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public DetalleFacturaService(FacturaRepository facturaRepository,
                               ProductoRepository productoRepository,
                               ServicioRepository servicioRepository,
                               ClienteRepository clienteRepository,
                               UsuarioRepository usuarioRepository) {
        this.facturaRepository = facturaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Método para verificar si el usuario tiene acceso a la factura
    private boolean verificarAccesoFactura(String facturaId, Authentication auth) {
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

    public List<DetalleFacturaDTO> getAllDetalles(String facturaId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para acceder a esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        if (factura.getDetalles() == null) {
            return new ArrayList<>();
        }
        
        return factura.getDetalles().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DetalleFacturaDTO getDetalleById(String facturaId, String detalleId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para acceder a esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        if (factura.getDetalles() == null) {
            throw new RuntimeException("Detalle no encontrado");
        }
        
        DetalleFactura detalle = factura.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        return convertToDTO(detalle);
    }

    public DetalleFacturaDTO createDetalle(String facturaId, DetalleFacturaDTO detalleDTO, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(UUID.randomUUID().toString()); // Generar un ID único
        
        // Verificar si se está agregando un producto o un servicio
        if (detalleDTO.getProductoId() != null) {
            detalle.setProductoId(detalleDTO.getProductoId());
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            detalle.setProductoNombre(producto.getNombre());
            
            // Calcular subtotal basado en el precio del producto y la cantidad
            BigDecimal precioProducto = producto.getPrecio();
            detalle.setSubtotal(precioProducto.multiply(new BigDecimal(detalleDTO.getCantidad())));
            
            // Actualizar el stock del producto
            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new RuntimeException("No hay suficiente stock del producto");
            }
            producto.setStock(producto.getStock() - detalleDTO.getCantidad());
            productoRepository.save(producto);
            
        } else if (detalleDTO.getServicioId() != null) {
            detalle.setServicioId(detalleDTO.getServicioId());
            Servicio servicio = servicioRepository.findById(detalleDTO.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            detalle.setServicioNombre(servicio.getNombre());
            
            // Calcular subtotal basado en el precio del servicio
            detalle.setSubtotal(servicio.getPrecio());
        } else {
            throw new RuntimeException("Debe especificar un producto o un servicio");
        }
        
        detalle.setCantidad(detalleDTO.getCantidad());
        
        // Añadir el detalle a la factura
        if (factura.getDetalles() == null) {
            factura.setDetalles(new ArrayList<>());
        }
        factura.getDetalles().add(detalle);
        
        // Recalcular el total de la factura
        calcularTotalFactura(factura);
        
        // Guardar la factura actualizada
        facturaRepository.save(factura);
        
        return convertToDTO(detalle);
    }

    public DetalleFacturaDTO updateDetalle(String facturaId, String detalleId, DetalleFacturaDTO detalleDTO, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        if (factura.getDetalles() == null) {
            throw new RuntimeException("Detalle no encontrado");
        }
        
        DetalleFactura detalle = factura.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        // Actualizar el producto si se proporciona
        if (detalleDTO.getProductoId() != null && !detalleDTO.getProductoId().equals(detalle.getProductoId())) {
            // Si cambia el producto, restaurar el stock del producto anterior
            if (detalle.getProductoId() != null) {
                Optional<Producto> productoAnterior = productoRepository.findById(detalle.getProductoId());
                productoAnterior.ifPresent(p -> {
                    p.setStock(p.getStock() + detalle.getCantidad());
                    productoRepository.save(p);
                });
            }
            
            Producto nuevoProducto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            detalle.setProductoId(nuevoProducto.getId());
            detalle.setProductoNombre(nuevoProducto.getNombre());
            
            // Restar el stock del nuevo producto
            if (nuevoProducto.getStock() < detalleDTO.getCantidad()) {
                throw new RuntimeException("No hay suficiente stock del producto");
            }
            nuevoProducto.setStock(nuevoProducto.getStock() - detalleDTO.getCantidad());
            productoRepository.save(nuevoProducto);
            
            // Recalcular subtotal
            BigDecimal precioProducto = nuevoProducto.getPrecio();
            detalle.setSubtotal(precioProducto.multiply(new BigDecimal(detalleDTO.getCantidad())));
            
            // Quitar servicio si había uno
            detalle.setServicioId(null);
            detalle.setServicioNombre(null);
        }
        
        // Actualizar el servicio si se proporciona
        if (detalleDTO.getServicioId() != null && !detalleDTO.getServicioId().equals(detalle.getServicioId())) {
            // Si cambia de producto a servicio, restaurar el stock del producto
            if (detalle.getProductoId() != null) {
                Optional<Producto> productoAnterior = productoRepository.findById(detalle.getProductoId());
                productoAnterior.ifPresent(p -> {
                    p.setStock(p.getStock() + detalle.getCantidad());
                    productoRepository.save(p);
                });
            }
            
            Servicio servicio = servicioRepository.findById(detalleDTO.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            
            detalle.setServicioId(servicio.getId());
            detalle.setServicioNombre(servicio.getNombre());
            
            // Recalcular subtotal para servicio
            detalle.setSubtotal(servicio.getPrecio());
            
            // Quitar producto si había uno
            detalle.setProductoId(null);
            detalle.setProductoNombre(null);
        }
        
        // Actualizar cantidad
        detalle.setCantidad(detalleDTO.getCantidad());
        
        // Recalcular total de la factura
        calcularTotalFactura(factura);
        
        // Guardar cambios
        facturaRepository.save(factura);
        
        return convertToDTO(detalle);
    }

    public void deleteDetalle(String facturaId, String detalleId, Authentication auth) {
        if (!verificarAccesoFactura(facturaId, auth)) {
            throw new RuntimeException("No tiene permiso para modificar esta factura");
        }
        
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        if (factura.getDetalles() == null) {
            throw new RuntimeException("Detalle no encontrado");
        }
        
        DetalleFactura detalle = factura.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));
        
        // Si es un producto, devolver stock
        if (detalle.getProductoId() != null) {
            Optional<Producto> producto = productoRepository.findById(detalle.getProductoId());
            producto.ifPresent(p -> {
                p.setStock(p.getStock() + detalle.getCantidad());
                productoRepository.save(p);
            });
        }
        
        // Eliminar el detalle
        factura.getDetalles().removeIf(d -> d.getId().equals(detalleId));
        
        // Recalcular total
        calcularTotalFactura(factura);
        
        // Guardar cambios
        facturaRepository.save(factura);
    }

    private void calcularTotalFactura(Factura factura) {
        BigDecimal total = factura.getDetalles() != null ? 
            factura.getDetalles().stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add) :
            BigDecimal.ZERO;
        
        factura.setTotal(total);
    }

    private DetalleFacturaDTO convertToDTO(DetalleFactura detalle) {
        DetalleFacturaDTO detalleDTO = new DetalleFacturaDTO();
        detalleDTO.setId(detalle.getId());
        detalleDTO.setProductoId(detalle.getProductoId());
        detalleDTO.setServicioId(detalle.getServicioId());
        detalleDTO.setCantidad(detalle.getCantidad());
        detalleDTO.setSubtotal(detalle.getSubtotal());
        
        return detalleDTO;
    }
}