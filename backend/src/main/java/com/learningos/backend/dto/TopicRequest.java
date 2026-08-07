package com.learningos.backend.dto;

import com.learningos.backend.entity.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TopicRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        @NotNull(message = "difficulty is required") Difficulty difficulty,
        @Min(value = 0, message = "estimatedMinutes must not be negative") int estimatedMinutes
) {
}
