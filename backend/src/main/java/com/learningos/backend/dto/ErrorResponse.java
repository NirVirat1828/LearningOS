package com.learningos.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Error envelope returned by {@link com.learningos.backend.exception.GlobalExceptionHandler}
 * for every unhandled or mapped exception, so error responses are shaped
 * consistently across the whole API.
 */
@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    @Builder.Default
    private final Instant timestamp = Instant.now();
}
