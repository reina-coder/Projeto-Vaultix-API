package br.com.fiap.vaultix.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " com id [" + id + "] não encontrado(a).");
    }
}
