package com.peluqueria.estructura.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Carrito {
    private String clienteId;
    private List<CarritoItem> items;
    private BigDecimal total;

    public Carrito(String clienteId) {
        this.clienteId = clienteId;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    // Método para añadir o actualizar un item
    public void agregarItem(CarritoItem item) {
        // Buscar si ya existe un item con ese productoId
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProductoId().equals(item.getProductoId())) {
                // Actualizar la cantidad
                items.get(i).setCantidad(items.get(i).getCantidad() + item.getCantidad());
                items.get(i).actualizarSubtotal();
                calcularTotal();
                return;
            }
        }

        // Si no existe, añadirlo
        items.add(item);
        calcularTotal();
    }

    // Método para actualizar la cantidad de un item
    public void actualizarItem(String productoId, int cantidad) {
        for (CarritoItem item : items) {
            if (item.getProductoId().equals(productoId)) {
                item.setCantidad(cantidad);
                item.actualizarSubtotal();
                calcularTotal();
                return;
            }
        }
    }

    // Método para eliminar un item
    public void eliminarItem(String productoId) {
        items.removeIf(item -> item.getProductoId().equals(productoId));
        calcularTotal();
    }

    // Método para calcular el total del carrito
    public void calcularTotal() {
        this.total = items.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para vaciar el carrito
    public void vaciar() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
    }
}
