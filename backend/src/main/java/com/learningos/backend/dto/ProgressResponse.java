package com.learningos.backend.dto;

import com.learningos.backend.entity.ProgressStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProgressResponse(
        UUID id,
        UUID topicId,
        String topicTitle,
        ProgressStatus status,
        int completionPercentage,
        LocalDate completedDate,
        Instant createdAt,
        Instant updatedAt
) {
}
