package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.RoadmapGraphResponse;
import com.learningos.backend.dto.RoadmapRequest;
import com.learningos.backend.dto.RoadmapResponse;
import com.learningos.backend.service.RoadmapGraphService;
import com.learningos.backend.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * CRUD for the top of the syllabus hierarchy. GET responses nest the full
 * tree (Roadmap -> Courses -> Modules -> Topics) via {@link RoadmapResponse}.
 */
@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final RoadmapGraphService roadmapGraphService;

    @GetMapping
    public ApiResponse<List<RoadmapResponse>> getAllRoadmaps() {
        return ApiResponse.success(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoadmapResponse> getRoadmap(@PathVariable UUID id) {
        return ApiResponse.success(roadmapService.getRoadmap(id));
    }

    @GetMapping("/{id}/graph")
    public ApiResponse<RoadmapGraphResponse> getRoadmapGraph(@PathVariable UUID id) {
        return ApiResponse.success(roadmapGraphService.getGraph(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoadmapResponse>> createRoadmap(@Valid @RequestBody RoadmapRequest request) {
        RoadmapResponse response = roadmapService.createRoadmap(request);
        return ResponseEntity.created(URI.create("/api/roadmaps/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoadmapResponse> updateRoadmap(
            @PathVariable UUID id, @Valid @RequestBody RoadmapRequest request) {
        return ApiResponse.success(roadmapService.updateRoadmap(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable UUID id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.noContent().build();
    }
}
