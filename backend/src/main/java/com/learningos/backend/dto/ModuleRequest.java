package com.learningos.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ModuleRequest(
        @NotBlank(message = "title is required") String title,
        String description
) {
}
