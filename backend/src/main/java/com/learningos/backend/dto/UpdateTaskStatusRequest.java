package com.learningos.backend.dto;

import com.learningos.backend.entity.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "status is required") TaskStatus status,
        @Min(value = 0, message = "timeSpentMinutes must not be negative") Integer timeSpentMinutes
) {
}
