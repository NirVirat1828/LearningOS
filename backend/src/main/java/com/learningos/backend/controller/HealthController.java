package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.HealthResponse;
import com.learningos.backend.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample endpoint proving the controller -> service -> dto slice of the
 * architecture is wired correctly end to end.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(healthService.checkHealth());
    }
}
