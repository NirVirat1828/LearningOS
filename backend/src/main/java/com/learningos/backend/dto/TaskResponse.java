package com.learningos.backend.dto;

import com.learningos.backend.entity.Priority;
import com.learningos.backend.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID topicId,
        String topicTitle,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate scheduledDate,
        LocalDate completedDate,
        Integer timeSpentMinutes,
        Instant createdAt,
        Instant updatedAt
) {
}
