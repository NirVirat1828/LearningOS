package com.learningos.backend.dto;

import com.learningos.backend.entity.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceRequest(
        @NotBlank(message = "title is required") String title,
        String url,
        @NotNull(message = "type is required") ResourceType type
) {
}
