package com.learningos.backend.exception;

/**
 * Thrown by service-layer code when a requested entity cannot be found.
 * Mapped to a 404 response by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
