package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.ModuleRequest;
import com.learningos.backend.dto.ModuleResponse;
import com.learningos.backend.service.ModuleService;
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
 * Module is created/listed under its parent Course's path, but read/updated/
 * deleted by its own id.
 */
@RestController
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping("/api/courses/{courseId}/modules")
    public ApiResponse<List<ModuleResponse>> getModulesByCourse(@PathVariable UUID courseId) {
        return ApiResponse.success(moduleService.getModulesByCourse(courseId));
    }

    @PostMapping("/api/courses/{courseId}/modules")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(
            @PathVariable UUID courseId, @Valid @RequestBody ModuleRequest request) {
        ModuleResponse response = moduleService.createModule(courseId, request);
        return ResponseEntity.created(URI.create("/api/modules/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/modules/{id}")
    public ApiResponse<ModuleResponse> getModule(@PathVariable UUID id) {
        return ApiResponse.success(moduleService.getModule(id));
    }

    @PutMapping("/api/modules/{id}")
    public ApiResponse<ModuleResponse> updateModule(
            @PathVariable UUID id, @Valid @RequestBody ModuleRequest request) {
        return ApiResponse.success(moduleService.updateModule(id, request));
    }

    @DeleteMapping("/api/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}
