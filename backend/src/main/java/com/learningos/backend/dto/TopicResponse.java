package com.learningos.backend.dto;

import com.learningos.backend.entity.Difficulty;
import com.learningos.backend.entity.ProgressStatus;

import java.time.Instant;
import java.util.UUID;

public record TopicResponse(
        UUID id,
        UUID moduleId,
        String title,
        String description,
        Difficulty difficulty,
        int estimatedMinutes,
        ProgressStatus completionStatus,
        int completionPercentage,
        Instant createdAt,
        Instant updatedAt
) {
}
