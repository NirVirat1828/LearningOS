package com.learningos.backend.service;

import com.learningos.backend.dto.CreateDependencyRequest;
import com.learningos.backend.dto.DependencyResponse;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.entity.TopicDependency;
import com.learningos.backend.exception.CircularDependencyException;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.repository.TopicDependencyRepository;
import com.learningos.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicDependencyService {

    private final TopicDependencyRepository topicDependencyRepository;
    private final TopicRepository topicRepository;

    public List<DependencyResponse> getDependenciesForTopic(UUID topicId) {
        ensureTopicExists(topicId);
        return topicDependencyRepository.findByTopicId(topicId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DependencyResponse createDependency(UUID topicId, CreateDependencyRequest request) {
        UUID dependsOnTopicId = request.dependsOnTopicId();
        if (topicId.equals(dependsOnTopicId)) {
            throw new CircularDependencyException("A topic cannot depend on itself.");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        Topic dependsOnTopic = topicRepository.findById(dependsOnTopicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + dependsOnTopicId));

        if (wouldCreateCycle(topicId, dependsOnTopicId)) {
            throw new CircularDependencyException(
                    "Adding this dependency would create a circular prerequisite chain.");
        }

        TopicDependency dependency = new TopicDependency(topic, dependsOnTopic);
        return toResponse(topicDependencyRepository.save(dependency));
    }

    @Transactional
    public void deleteDependency(UUID id) {
        if (!topicDependencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dependency not found: " + id);
        }
        topicDependencyRepository.deleteById(id);
    }

    /**
     * Would adding "topicId requires dependsOnTopicId" close a cycle?
     * True iff dependsOnTopicId is already reachable from topicId by walking
     * forward along existing prerequisite edges (from a topic to the topics
     * that already depend on it). Reaching it means dependsOnTopicId already
     * (transitively) requires topicId, so requiring it back would close the
     * loop: topicId -> ... -> dependsOnTopicId -> topicId.
     */
    private boolean wouldCreateCycle(UUID topicId, UUID dependsOnTopicId) {
        Map<UUID, List<UUID>> dependentsOf = new HashMap<>();
        for (TopicDependency edge : topicDependencyRepository.findAll()) {
            dependentsOf.computeIfAbsent(edge.getDependsOnTopic().getId(), id -> new ArrayList<>())
                    .add(edge.getTopic().getId());
        }

        Deque<UUID> queue = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(topicId);
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(dependsOnTopicId)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }
            queue.addAll(dependentsOf.getOrDefault(current, List.of()));
        }
        return false;
    }

    private void ensureTopicExists(UUID topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException("Topic not found: " + topicId);
        }
    }

    private DependencyResponse toResponse(TopicDependency dependency) {
        return new DependencyResponse(
                dependency.getId(),
                dependency.getTopic().getId(),
                dependency.getTopic().getTitle(),
                dependency.getDependsOnTopic().getId(),
                dependency.getDependsOnTopic().getTitle()
        );
    }
}
