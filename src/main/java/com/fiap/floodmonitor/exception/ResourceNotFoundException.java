package com.fiap.floodmonitor.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " com id " + id + " não encontrado.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
