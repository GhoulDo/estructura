package com.peluqueria.estructura.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, String fieldName, String fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", entityName, fieldName, fieldValue));
    }
}
