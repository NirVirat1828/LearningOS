package com.learningos.backend.service;

import com.learningos.backend.dto.HealthResponse;
import org.springframework.stereotype.Service;

/**
 * Holds the business logic behind the health endpoint, kept separate from
 * {@link com.learningos.backend.controller.HealthController} so the
 * controller stays a thin HTTP adapter.
 */
@Service
public class HealthService {

    public HealthResponse checkHealth() {
        return HealthResponse.builder()
                .status("UP")
                .service("learningos-backend")
                .build();
    }
}
