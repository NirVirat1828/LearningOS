package com.learningos.backend.dto;

/**
 * Derived (not persisted) status of a Topic within a roadmap's dependency
 * graph, computed by {@link com.learningos.backend.service.RoadmapGraphService}
 * from the topic's own Progress plus its direct prerequisites' Progress.
 */
public enum NodeStatus {
    LOCKED,
    UNLOCKED,
    CURRENT,
    COMPLETED
}
