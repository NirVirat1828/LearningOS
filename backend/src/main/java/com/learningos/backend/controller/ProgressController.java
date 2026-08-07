package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.ProgressResponse;
import com.learningos.backend.dto.UpdateProgressRequest;
import com.learningos.backend.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Progress is a 1:1 companion to Topic (created automatically when a Topic
 * is created), so there's no create/delete here -- only read and update.
 */
@RestController
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/api/topics/{topicId}/progress")
    public ApiResponse<ProgressResponse> getProgress(@PathVariable UUID topicId) {
        return ApiResponse.success(progressService.getProgressForTopic(topicId));
    }

    @PatchMapping("/api/topics/{topicId}/progress")
    public ApiResponse<ProgressResponse> updateProgress(
            @PathVariable UUID topicId, @Valid @RequestBody UpdateProgressRequest request) {
        return ApiResponse.success(progressService.updateProgress(topicId, request));
    }
}
