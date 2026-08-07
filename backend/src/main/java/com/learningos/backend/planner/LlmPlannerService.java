package com.learningos.backend.planner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.entity.Priority;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LLM-backed planner — active when {@code spring.profiles.active=llm}.
 *
 * <p>Builds a context-rich prompt from the live task backlog and delegates
 * the prioritisation decision to the configured {@link LlmClient} strategy.
 * Because the caller only knows about {@link LlmClient} (the interface), the
 * underlying provider (Gemini, OpenAI, Ollama…) can be swapped at any time
 * without touching this class.</p>
 *
 * <p><b>Strategy pattern in action:</b><br>
 * {@code LlmPlannerService} is the <em>Context</em>. {@link LlmClient} is the
 * <em>Strategy</em>. {@link GeminiClient} (or any future class) is the
 * <em>Concrete Strategy</em>.  Spring's DI container selects and injects the
 * concrete strategy at startup — this class never instantiates or references
 * Gemini directly.</p>
 */
@Service
@Profile("llm")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LlmPlannerService implements PlannerService {

    private static final Logger log = LoggerFactory.getLogger(LlmPlannerService.class);

    private final TaskRepository taskRepository;
    private final LlmClient llmClient;       // ← injected strategy; could be Gemini, OpenAI, etc.
    private final ObjectMapper objectMapper;

    @Override
    public List<TaskResponse> generateTodaysTasks() {
        // 1. Gather backlog context.
        List<Map<String, String>> backlog = taskRepository
                .findByScheduledDateIsNullAndStatus(com.learningos.backend.entity.TaskStatus.PENDING)
                .stream()
                .map(t -> Map.of(
                        "id",          t.getId().toString(),
                        "title",       t.getTitle(),
                        "priority",    t.getPriority().name(),
                        "description", t.getDescription() != null ? t.getDescription() : ""
                ))
                .collect(Collectors.toList());

        if (backlog.isEmpty()) {
            log.info("Backlog is empty — nothing to plan.");
            return Collections.emptyList();
        }

        // 2. Build a structured prompt.
        String prompt = buildPrompt(backlog);
        log.debug("Sending planning prompt to LLM.");

        // 3. Ask the LLM to select and order up to 5 task IDs.
        String rawResponse = llmClient.chat(prompt);
        log.debug("LLM raw response: {}", rawResponse);

        // 4. Parse the JSON array of task IDs returned by the LLM.
        List<String> selectedIds = parseIds(rawResponse);

        // 5. Convert selected task IDs to TaskResponse DTOs (preserving LLM order).
        LocalDate today = LocalDate.now();
        return selectedIds.stream()
                .map(id -> taskRepository.findById(UUID.fromString(id)).orElse(null))
                .filter(t -> t != null)
                .map(t -> new TaskResponse(
                        t.getId(),
                        t.getTopic().getId(),
                        t.getTopic().getTitle(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getStatus(),
                        t.getPriority(),
                        today,
                        t.getCompletedDate(),
                        t.getTimeSpentMinutes(),
                        t.getCreatedAt(),
                        t.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String buildPrompt(List<Map<String, String>> backlog) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an intelligent study planner. Given the following task backlog, ");
        sb.append("select and order up to 5 tasks for today. Consider: priority (HIGH > MEDIUM > LOW), ");
        sb.append("topic difficulty (harder topics benefit from shorter sessions), and variety.\n\n");
        sb.append("Backlog (JSON):\n");
        try {
            sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backlog));
        } catch (Exception e) {
            sb.append(backlog.toString());
        }
        sb.append("\n\nRespond with ONLY a JSON array of task IDs to schedule today, e.g.:\n");
        sb.append("[\"uuid-1\", \"uuid-2\", \"uuid-3\"]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> parseIds(String raw) {
        try {
            // Strip markdown code fences if the LLM wraps the response.
            String cleaned = raw.replaceAll("(?s)```json|```", "").trim();
            return objectMapper.readValue(cleaned, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Could not parse LLM response as JSON array of IDs: {}", raw, e);
            return Collections.emptyList();
        }
    }
}
