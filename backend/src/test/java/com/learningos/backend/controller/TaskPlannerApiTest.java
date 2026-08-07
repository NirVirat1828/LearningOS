package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the daily planner algorithm against the seeded task pool:
 * 1 overdue PENDING task (carries forward), 1 already-today PENDING task,
 * 3 backlog PENDING tasks (HIGH/MEDIUM/LOW), 1 SKIPPED task in the past
 * (must NOT carry forward), and daily-capacity 5 -- chosen so the seeded
 * data fills exactly to capacity, proving both carry-forward and backlog
 * fill-up ran.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskPlannerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateCarriesForwardOverdueTasksAndFillsBacklogByPriority() throws Exception {
        String beforeJson = mockMvc.perform(get("/api/tasks/today"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Only "Review React Router basics" was already scheduled for today.
        assertThat(objectMapper.readTree(beforeJson).get("data")).hasSize(1);

        String generatedJson = mockMvc.perform(post("/api/tasks/today/generate"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode tasks = objectMapper.readTree(generatedJson).get("data");

        // 1 already-today + 1 carried-forward + 3 backlog pulled in = 5, hitting the configured cap exactly.
        assertThat(tasks).hasSize(5);
        List<String> titles = StreamSupport.stream(tasks.spliterator(), false)
                .map(t -> t.get("title").asText())
                .toList();
        assertThat(titles).containsExactlyInAnyOrder(
                "Review React Router basics",
                "Write a @Service/@Autowired example",
                "Build a reusable Button component",
                "Build a sample REST controller",
                "Model a one-to-many relationship"
        );

        // The SKIPPED task from yesterday must not have carried forward.
        assertThat(titles).doesNotContain("Watch the Spring DI conference talk");

        String afterJson = mockMvc.perform(get("/api/tasks/today"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(afterJson).get("data")).hasSize(5);
    }

    @Test
    void backlogAfterGenerationExcludesEverythingPulledIn() throws Exception {
        mockMvc.perform(post("/api/tasks/today/generate")).andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void backlogIsOrderedByPriorityThenAge() throws Exception {
        String backlogJson = mockMvc.perform(get("/api/tasks/backlog"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode backlog = objectMapper.readTree(backlogJson).get("data");

        List<String> priorities = StreamSupport.stream(backlog.spliterator(), false)
                .map(t -> t.get("priority").asText())
                .toList();
        // Must be severity order (HIGH, MEDIUM, LOW), not alphabetical
        // ("HIGH", "LOW", "MEDIUM") -- which is exactly what a naive DB
        // ORDER BY on the STRING-mapped priority column would produce.
        assertThat(priorities).containsExactly("HIGH", "MEDIUM", "LOW");
        assertThat(backlog.get(0).get("title").asText()).isEqualTo("Build a reusable Button component");
    }

    @Test
    void togglingStatusViaPatchMovesTaskBetweenSections() throws Exception {
        String todayJson = mockMvc.perform(post("/api/tasks/today/generate"))
                .andReturn().getResponse().getContentAsString();
        String taskId = objectMapper.readTree(todayJson).get("data").get(0).get("id").asText();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .contentType("application/json")
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                        .contentType("application/json")
                        .content("{\"status\":\"SKIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SKIPPED"));
    }

    @Test
    void updatingUnknownTaskReturns404() throws Exception {
        mockMvc.perform(patch("/api/tasks/" + java.util.UUID.randomUUID() + "/status")
                        .contentType("application/json")
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound());
    }
}
