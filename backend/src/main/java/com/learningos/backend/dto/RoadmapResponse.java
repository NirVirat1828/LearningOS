package com.learningos.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        String title,
        String description,
        List<CourseResponse> courses,
        Instant createdAt,
        Instant updatedAt
) {
}
