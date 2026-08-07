package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.ResourceRequest;
import com.learningos.backend.dto.ResourceResponse;
import com.learningos.backend.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
 * Resource is created/listed under its parent Topic's path, but read/
 * updated/deleted by its own id.
 */
@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/api/topics/{topicId}/resources")
    public ApiResponse<List<ResourceResponse>> getResourcesByTopic(@PathVariable UUID topicId) {
        return ApiResponse.success(resourceService.getResourcesByTopic(topicId));
    }

    @PostMapping("/api/topics/{topicId}/resources")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @PathVariable UUID topicId, @Valid @RequestBody ResourceRequest request) {
        ResourceResponse response = resourceService.createResource(topicId, request);
        return ResponseEntity.created(URI.create("/api/resources/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/resources/{id}")
    public ApiResponse<ResourceResponse> getResource(@PathVariable UUID id) {
        return ApiResponse.success(resourceService.getResource(id));
    }

    @PutMapping("/api/resources/{id}")
    public ApiResponse<ResourceResponse> updateResource(
            @PathVariable UUID id, @Valid @RequestBody ResourceRequest request) {
        return ApiResponse.success(resourceService.updateResource(id, request));
    }

    @DeleteMapping("/api/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
