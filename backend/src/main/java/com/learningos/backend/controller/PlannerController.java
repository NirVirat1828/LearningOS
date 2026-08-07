package com.learningos.backend.controller;

import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.planner.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoint for the AI Planner.
 *
 * <p>GET /api/planner/today
 *
 * <p>The concrete {@link PlannerService} implementation is injected by Spring
 * based on the active profile ({@code rule-based} or {@code llm}).  This
 * controller is completely unaware of which implementation is active.</p>
 */
@RestController
@RequestMapping("/api/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    /**
     * Generate today's task plan.
     *
     * @return 200 OK with a list of today's tasks.
     */
    @GetMapping("/today")
    public ResponseEntity<List<TaskResponse>> today() {
        return ResponseEntity.ok(plannerService.generateTodaysTasks());
    }
}
