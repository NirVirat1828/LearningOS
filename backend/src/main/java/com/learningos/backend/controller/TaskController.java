package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.dto.UpdateTaskStatusRequest;
import com.learningos.backend.service.TaskPlannerService;
import com.learningos.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskPlannerService taskPlannerService;

    /** Pure read of whatever is currently scheduled for today — no side effects. */
    @GetMapping("/today")
    public ApiResponse<List<TaskResponse>> getTodaysTasks() {
        return ApiResponse.success(taskService.getTodaysTasks());
    }

    /** Runs the planner algorithm (carry forward + backlog fill-up) and returns the result. */
    @PostMapping("/today/generate")
    public ApiResponse<List<TaskResponse>> generateTodaysTasks() {
        return ApiResponse.success(taskPlannerService.generateTodaysTasks());
    }

    @GetMapping("/backlog")
    public ApiResponse<List<TaskResponse>> getBacklog() {
        return ApiResponse.success(taskService.getBacklog());
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TaskResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ApiResponse.success(taskService.updateStatus(id, request));
    }
}
