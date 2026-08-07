package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.CourseRequest;
import com.learningos.backend.dto.CourseResponse;
import com.learningos.backend.service.CourseService;
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
 * Course is created/listed under its parent Roadmap's path, but read/updated/
 * deleted by its own id, since a Course id is already globally unique.
 */
@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/api/roadmaps/{roadmapId}/courses")
    public ApiResponse<List<CourseResponse>> getCoursesByRoadmap(@PathVariable UUID roadmapId) {
        return ApiResponse.success(courseService.getCoursesByRoadmap(roadmapId));
    }

    @PostMapping("/api/roadmaps/{roadmapId}/courses")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @PathVariable UUID roadmapId, @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(roadmapId, request);
        return ResponseEntity.created(URI.create("/api/courses/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/courses/{id}")
    public ApiResponse<CourseResponse> getCourse(@PathVariable UUID id) {
        return ApiResponse.success(courseService.getCourse(id));
    }

    @PutMapping("/api/courses/{id}")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
