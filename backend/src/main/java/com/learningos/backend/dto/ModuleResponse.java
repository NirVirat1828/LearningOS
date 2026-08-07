package com.learningos.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        UUID courseId,
        String title,
        String description,
        List<TopicResponse> topics,
        Instant createdAt,
        Instant updatedAt
) {
}
