package com.peluqueria.estructura.dto;

import com.peluqueria.estructura.entity.CarritoItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutResumenDTO {
    private List<CarritoItem> items;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String clienteNombre;
    private String clienteEmail;
    private String direccionEntrega;
    private boolean stockDisponible;
}
