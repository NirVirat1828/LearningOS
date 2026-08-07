package com.learningos.backend.planner;

/**
 * Thrown when the Gemini REST API returns an unexpected response or
 * a non-2xx HTTP status.
 */
public class GeminiException extends RuntimeException {

    public GeminiException(String message) {
        super(message);
    }

    public GeminiException(String message, Throwable cause) {
        super(message, cause);
    }
}
