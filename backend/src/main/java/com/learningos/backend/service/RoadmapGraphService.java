package com.learningos.backend.service;

import com.learningos.backend.dto.GraphEdge;
import com.learningos.backend.dto.GraphNode;
import com.learningos.backend.dto.NodeStatus;
import com.learningos.backend.dto.RoadmapGraphResponse;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.entity.TopicDependency;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.repository.RoadmapRepository;
import com.learningos.backend.repository.TopicDependencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds the dependency graph for one Roadmap: every Topic across all its
 * Courses/Modules as a node, every TopicDependency edge between them, and
 * a derived {@link NodeStatus} per node. See {@link #computeStatus} for the
 * status algorithm.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapGraphService {

    private final RoadmapRepository roadmapRepository;
    private final TopicDependencyRepository topicDependencyRepository;

    public RoadmapGraphResponse getGraph(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        List<Topic> topics = roadmap.getCourses().stream()
                .flatMap(course -> course.getModules().stream())
                .flatMap(module -> module.getTopics().stream())
                .toList();
        Set<UUID> topicIds = topics.stream().map(Topic::getId).collect(Collectors.toSet());

        // Scope edges to this roadmap: both endpoints must be one of its own topics.
        List<TopicDependency> edges = topicDependencyRepository.findAll().stream()
                .filter(dep -> topicIds.contains(dep.getTopic().getId())
                        && topicIds.contains(dep.getDependsOnTopic().getId()))
                .toList();

        Map<UUID, List<UUID>> prerequisitesOf = new HashMap<>();
        for (TopicDependency edge : edges) {
            prerequisitesOf.computeIfAbsent(edge.getTopic().getId(), id -> new ArrayList<>())
                    .add(edge.getDependsOnTopic().getId());
        }

        Map<UUID, ProgressStatus> progressByTopicId = topics.stream()
                .collect(Collectors.toMap(Topic::getId, this::ownProgressStatus));

        List<GraphNode> nodes = topics.stream()
                .map(topic -> new GraphNode(
                        topic.getId(),
                        topic.getTitle(),
                        topic.getDifficulty(),
                        topic.getEstimatedMinutes(),
                        topic.getModule().getId(),
                        computeStatus(topic.getId(), prerequisitesOf, progressByTopicId)))
                .toList();

        List<GraphEdge> graphEdges = edges.stream()
                .map(edge -> new GraphEdge(edge.getId(), edge.getDependsOnTopic().getId(), edge.getTopic().getId()))
                .toList();

        return new RoadmapGraphResponse(roadmapId, nodes, graphEdges);
    }

    /**
     * COMPLETED/CURRENT come straight from the topic's own Progress. A
     * NOT_STARTED topic is UNLOCKED if every *direct* prerequisite is
     * COMPLETED (or it has none), otherwise LOCKED. Checking only direct
     * prerequisites is sufficient — a prerequisite can't itself be COMPLETED
     * without its own prerequisites already being satisfied by the same
     * rule, so the chain enforces itself level by level without needing a
     * full transitive-closure walk.
     */
    private NodeStatus computeStatus(
            UUID topicId, Map<UUID, List<UUID>> prerequisitesOf, Map<UUID, ProgressStatus> progressByTopicId) {
        ProgressStatus ownStatus = progressByTopicId.get(topicId);
        if (ownStatus == ProgressStatus.COMPLETED) {
            return NodeStatus.COMPLETED;
        }
        if (ownStatus == ProgressStatus.IN_PROGRESS) {
            return NodeStatus.CURRENT;
        }
        List<UUID> prerequisites = prerequisitesOf.getOrDefault(topicId, List.of());
        boolean allPrerequisitesComplete = prerequisites.stream()
                .allMatch(id -> progressByTopicId.get(id) == ProgressStatus.COMPLETED);
        return allPrerequisitesComplete ? NodeStatus.UNLOCKED : NodeStatus.LOCKED;
    }

    private ProgressStatus ownProgressStatus(Topic topic) {
        return topic.getProgress() != null ? topic.getProgress().getStatus() : ProgressStatus.NOT_STARTED;
    }
}
