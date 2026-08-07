package com.learningos.backend.dto;

import java.util.UUID;

public record DependencyResponse(
        UUID id,
        UUID topicId,
        String topicTitle,
        UUID dependsOnTopicId,
        String dependsOnTopicTitle
) {
}
