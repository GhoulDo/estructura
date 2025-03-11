// src/main/java/com/peluqueria/estructura/service/FacturaService.java
package com.peluqueria.estructura.service;

import com.peluqueria.estructura.dto.FacturaDTO;
import com.peluqueria.estructura.entity.Cliente;
import com.peluqueria.estructura.entity.Factura;
import com.peluqueria.estructura.entity.Usuario;
import com.peluqueria.estructura.repository.ClienteRepository;
import com.peluqueria.estructura.repository.FacturaRepository;
import com.peluqueria.estructura.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public FacturaService(FacturaRepository facturaRepository, 
                         ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository) {
        this.facturaRepository = facturaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<FacturaDTO> getAllFacturas(Authentication auth) {
        List<Factura> facturas;
        
        // Si es ADMIN, puede ver todas las facturas
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            facturas = facturaRepository.findAll();
        } else {
            // Si es CLIENTE, solo ve sus propias facturas
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
            facturas = facturaRepository.findByClienteId(cliente.getId());
        }
        
        return facturas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public FacturaDTO getFacturaById(Long id, Authentication auth) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        // Si es CLIENTE, verificar que la factura le pertenezca
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
            if (!factura.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("No tiene permiso para ver esta factura");
            }
        }
        
        return convertToDTO(factura);
    }

    public FacturaDTO createFactura(FacturaDTO facturaDTO, Authentication auth) {
        Factura factura = new Factura();
        
        // Si es CLIENTE, asignar automáticamente su cliente
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
            factura.setCliente(cliente);
        } else {
            // Si es ADMIN, usar el cliente proporcionado
            Cliente cliente = clienteRepository.findById(facturaDTO.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            factura.setCliente(cliente);
        }
        
        factura.setFecha(facturaDTO.getFecha());
        factura.setTotal(BigDecimal.ZERO); // Se calculará al agregar detalles
        
        Factura savedFactura = facturaRepository.save(factura);
        return convertToDTO(savedFactura);
    }

    public BigDecimal calcularTotalFactura(Long facturaId, Authentication auth) {
        // Verificar acceso primero
        FacturaDTO factura = getFacturaById(facturaId, auth);
        
        Factura facturaEntity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        BigDecimal total = facturaEntity.getDetalles().stream()
                .map(detalle -> detalle.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Actualizar el total en la factura
        facturaEntity.setTotal(total);
        facturaRepository.save(facturaEntity);
        
        return total;
    }
    
    private FacturaDTO convertToDTO(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setClienteId(factura.getCliente().getId());
        dto.setClienteNombre(factura.getCliente().getNombre());
        dto.setFecha(factura.getFecha());
        dto.setTotal(factura.getTotal());
        return dto;
    }
}