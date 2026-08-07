package com.learningos.backend.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Payload returned by the sample health endpoint, proving the
 * controller -> service -> dto slice of the layered architecture works
 * end to end.
 */
@Getter
@Builder
public class HealthResponse {

    private final String status;
    private final String service;
}
