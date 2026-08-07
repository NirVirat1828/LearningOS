package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.CreateDependencyRequest;
import com.learningos.backend.dto.DependencyResponse;
import com.learningos.backend.service.TopicDependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * A dependency edge has no meaningful fields to update in place (it's an
 * identity: which topic requires which), so this exposes create/read/delete
 * only, not a full CRUD surface.
 */
@RestController
@RequiredArgsConstructor
public class TopicDependencyController {

    private final TopicDependencyService topicDependencyService;

    @GetMapping("/api/topics/{topicId}/dependencies")
    public ApiResponse<List<DependencyResponse>> getDependencies(@PathVariable UUID topicId) {
        return ApiResponse.success(topicDependencyService.getDependenciesForTopic(topicId));
    }

    @PostMapping("/api/topics/{topicId}/dependencies")
    public ResponseEntity<ApiResponse<DependencyResponse>> createDependency(
            @PathVariable UUID topicId, @Valid @RequestBody CreateDependencyRequest request) {
        DependencyResponse response = topicDependencyService.createDependency(topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/api/dependencies/{id}")
    public ResponseEntity<Void> deleteDependency(@PathVariable UUID id) {
        topicDependencyService.deleteDependency(id);
        return ResponseEntity.noContent().build();
    }
}
