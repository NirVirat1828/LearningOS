package com.learningos.backend.exception;

/**
 * Thrown when creating a TopicDependency edge would close a cycle in the
 * prerequisite graph (including a topic depending on itself). Mapped to a
 * 409 response by {@link GlobalExceptionHandler}.
 */
public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(String message) {
        super(message);
    }
}
