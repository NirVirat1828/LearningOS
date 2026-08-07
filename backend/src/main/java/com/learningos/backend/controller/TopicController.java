package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.TopicRequest;
import com.learningos.backend.dto.TopicResponse;
import com.learningos.backend.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Topic is created/listed under its parent Module's path, but read/updated/
 * deleted by its own id. Topic is the leaf of the syllabus tree, so
 * TopicResponse carries the display fields (difficulty, estimatedMinutes,
 * completionStatus) instead of nesting further children.
 */
@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/api/modules/{moduleId}/topics")
    public ApiResponse<List<TopicResponse>> getTopicsByModule(@PathVariable UUID moduleId) {
        return ApiResponse.success(topicService.getTopicsByModule(moduleId));
    }

    @PostMapping("/api/modules/{moduleId}/topics")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @PathVariable UUID moduleId, @Valid @RequestBody TopicRequest request) {
        TopicResponse response = topicService.createTopic(moduleId, request);
        return ResponseEntity.created(URI.create("/api/topics/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/topics/{id}")
    public ApiResponse<TopicResponse> getTopic(@PathVariable UUID id) {
        return ApiResponse.success(topicService.getTopic(id));
    }

    @PutMapping("/api/topics/{id}")
    public ApiResponse<TopicResponse> updateTopic(
            @PathVariable UUID id, @Valid @RequestBody TopicRequest request) {
        return ApiResponse.success(topicService.updateTopic(id, request));
    }

    @DeleteMapping("/api/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}
