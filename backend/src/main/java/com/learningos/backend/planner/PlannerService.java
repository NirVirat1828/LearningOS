package com.learningos.backend.planner;

import com.learningos.backend.dto.TaskResponse;

import java.util.List;

/**
 * Strategy interface for generating today's task plan.
 *
 * <p>The application selects the concrete implementation via Spring Profiles:
 * <ul>
 *   <li>{@code rule-based} — uses deterministic rules (priority, difficulty, backlog, time)</li>
 *   <li>{@code llm}         — delegates to an LLM provider via the {@link LlmClient} strategy</li>
 * </ul>
 *
 * <p>Adding a third strategy (e.g. ML-based) requires only a new class that implements
 * this interface and is annotated {@code @Profile("ml")} — nothing else changes.
 */
public interface PlannerService {

    /**
     * Generate today's task plan.
     *
     * @return ordered list of tasks to work on today.
     */
    List<TaskResponse> generateTodaysTasks();
}
