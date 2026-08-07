package com.learningos.backend.dto;

import com.learningos.backend.entity.Difficulty;

import java.util.UUID;

public record GraphNode(
        UUID id,
        String title,
        Difficulty difficulty,
        int estimatedMinutes,
        UUID moduleId,
        NodeStatus status
) {
}
