package com.learningos.backend.dto;

import java.util.List;
import java.util.UUID;

public record RoadmapGraphResponse(
        UUID roadmapId,
        List<GraphNode> nodes,
        List<GraphEdge> edges
) {
}
