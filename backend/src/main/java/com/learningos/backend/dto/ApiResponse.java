package com.learningos.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standard envelope every controller endpoint responds with, so the frontend
 * can rely on one consistent response shape ({@code success}, {@code data},
 * {@code timestamp}) regardless of which endpoint it calls.
 */
@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
}
