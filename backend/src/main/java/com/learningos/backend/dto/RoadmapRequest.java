package com.learningos.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RoadmapRequest(
        @NotBlank(message = "title is required") String title,
        String description
) {
}
