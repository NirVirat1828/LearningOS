package com.learningos.backend.dto;

import com.learningos.backend.entity.ProgressStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProgressRequest(
        @NotNull(message = "status is required") ProgressStatus status,
        @Min(value = 0, message = "completionPercentage must be between 0 and 100")
        @Max(value = 100, message = "completionPercentage must be between 0 and 100")
        int completionPercentage
) {
}
