package com.learningos.backend.dto;

import com.learningos.backend.entity.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        UUID topicId,
        String title,
        String url,
        ResourceType type,
        Instant createdAt,
        Instant updatedAt
) {
}
