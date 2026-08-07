package com.learningos.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDependencyRequest(
        @NotNull(message = "dependsOnTopicId is required") UUID dependsOnTopicId
) {
}
