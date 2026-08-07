package com.learningos.backend.dto;

import java.util.UUID;

/**
 * {@code source}/{@code target} match React Flow's edge shape directly, so
 * the frontend can pass these straight through. {@code source} is the
 * prerequisite topic (dependsOnTopic); {@code target} is the topic that
 * requires it — the edge points in the direction work must flow.
 */
public record GraphEdge(
        UUID id,
        UUID source,
        UUID target
) {
}
