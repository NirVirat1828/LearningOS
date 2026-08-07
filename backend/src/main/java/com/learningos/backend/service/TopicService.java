package com.learningos.backend.service;

import com.learningos.backend.dto.TopicRequest;
import com.learningos.backend.dto.TopicResponse;
import com.learningos.backend.entity.Module;
import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.SyllabusMapper;
import com.learningos.backend.repository.ModuleRepository;
import com.learningos.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final ModuleRepository moduleRepository;

    public List<TopicResponse> getTopicsByModule(UUID moduleId) {
        ensureModuleExists(moduleId);
        return topicRepository.findByModuleId(moduleId).stream().map(SyllabusMapper::toResponse).toList();
    }

    public TopicResponse getTopic(UUID id) {
        return SyllabusMapper.toResponse(findTopicOrThrow(id));
    }

    @Transactional
    public TopicResponse createTopic(UUID moduleId, TopicRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        Topic topic = new Topic();
        topic.setTitle(request.title());
        topic.setDescription(request.description());
        topic.setDifficulty(request.difficulty());
        topic.setEstimatedMinutes(request.estimatedMinutes());
        module.addTopic(topic);

        // Every Topic gets a Progress row up front, so completionStatus is
        // always present in TopicResponse instead of needing a null case.
        Progress progress = new Progress();
        progress.setStatus(ProgressStatus.NOT_STARTED);
        progress.setCompletionPercentage(0);
        topic.setProgress(progress);

        return SyllabusMapper.toResponse(topicRepository.save(topic));
    }

    @Transactional
    public TopicResponse updateTopic(UUID id, TopicRequest request) {
        Topic topic = findTopicOrThrow(id);
        topic.setTitle(request.title());
        topic.setDescription(request.description());
        topic.setDifficulty(request.difficulty());
        topic.setEstimatedMinutes(request.estimatedMinutes());
        return SyllabusMapper.toResponse(topic);
    }

    @Transactional
    public void deleteTopic(UUID id) {
        topicRepository.delete(findTopicOrThrow(id));
    }

    private void ensureModuleExists(UUID moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module not found: " + moduleId);
        }
    }

    private Topic findTopicOrThrow(UUID id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id));
    }
}
