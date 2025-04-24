package com.peluqueria.estructura.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CarritoItem {
    private String productoId;
    private String nombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Constructor vacío requerido por algunos frameworks
    public CarritoItem() {
    }

    // Constructor para crear un item a partir de un producto
    public CarritoItem(Producto producto, int cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.subtotal = producto.getPrecio().multiply(new BigDecimal(cantidad));
    }

    // Método para actualizar el subtotal
    public void actualizarSubtotal() {
        this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
    }
}
