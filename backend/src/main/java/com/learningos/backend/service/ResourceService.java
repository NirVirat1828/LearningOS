package com.learningos.backend.service;

import com.learningos.backend.dto.ResourceRequest;
import com.learningos.backend.dto.ResourceResponse;
import com.learningos.backend.entity.Resource;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.ResourceMapper;
import com.learningos.backend.repository.ResourceRepository;
import com.learningos.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final TopicRepository topicRepository;

    public List<ResourceResponse> getResourcesByTopic(UUID topicId) {
        ensureTopicExists(topicId);
        return resourceRepository.findByTopicId(topicId).stream()
                .map(ResourceMapper::toResponse)
                .toList();
    }

    public ResourceResponse getResource(UUID id) {
        return ResourceMapper.toResponse(findResourceOrThrow(id));
    }

    @Transactional
    public ResourceResponse createResource(UUID topicId, ResourceRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));
        Resource resource = new Resource();
        resource.setTitle(request.title());
        resource.setUrl(request.url());
        resource.setType(request.type());
        topic.addResource(resource);
        return ResourceMapper.toResponse(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse updateResource(UUID id, ResourceRequest request) {
        Resource resource = findResourceOrThrow(id);
        resource.setTitle(request.title());
        resource.setUrl(request.url());
        resource.setType(request.type());
        return ResourceMapper.toResponse(resource);
    }

    @Transactional
    public void deleteResource(UUID id) {
        resourceRepository.delete(findResourceOrThrow(id));
    }

    private void ensureTopicExists(UUID topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException("Topic not found: " + topicId);
        }
    }

    private Resource findResourceOrThrow(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }
}
