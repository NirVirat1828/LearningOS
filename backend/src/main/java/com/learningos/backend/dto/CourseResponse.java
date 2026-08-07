package com.learningos.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        UUID roadmapId,
        String title,
        String description,
        List<ModuleResponse> modules,
        Instant createdAt,
        Instant updatedAt
) {
}
