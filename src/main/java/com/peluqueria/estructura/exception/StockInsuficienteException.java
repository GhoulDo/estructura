package com.peluqueria.estructura.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String productoNombre, int disponible, int solicitado) {
        super(String.format("Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
                productoNombre, disponible, solicitado));
    }
}
