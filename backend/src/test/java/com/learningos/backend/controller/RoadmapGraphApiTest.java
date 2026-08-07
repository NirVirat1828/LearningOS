package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the seeded roadmap's dependency graph resolves to the expected
 * mix of node statuses: COMPLETED -> CURRENT -> LOCKED -> LOCKED chain, plus
 * one UNLOCKED topic with no prerequisites at all.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoadmapGraphApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void graphReflectsSeededProgressAndPrerequisiteChain() throws Exception {
        String roadmapsJson = mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String roadmapId = objectMapper.readTree(roadmapsJson).get("data").get(0).get("id").asText();

        String graphJson = mockMvc.perform(get("/api/roadmaps/" + roadmapId + "/graph"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode data = objectMapper.readTree(graphJson).get("data");
        assertThat(data.get("roadmapId").asText()).isEqualTo(roadmapId);

        JsonNode nodes = data.get("nodes");
        JsonNode edges = data.get("edges");
        assertThat(nodes).hasSize(5);
        assertThat(edges).hasSize(3);

        Map<String, String> statusByTitle = new HashMap<>();
        StreamSupport.stream(nodes.spliterator(), false)
                .forEach(node -> statusByTitle.put(node.get("title").asText(), node.get("status").asText()));

        assertThat(statusByTitle.get("Java & Spring Fundamentals")).isEqualTo("COMPLETED");
        assertThat(statusByTitle.get("Dependency Injection")).isEqualTo("CURRENT");
        assertThat(statusByTitle.get("REST Controllers")).isEqualTo("LOCKED");
        assertThat(statusByTitle.get("Entity Mapping & Relationships")).isEqualTo("LOCKED");
        assertThat(statusByTitle.get("Components & Props")).isEqualTo("UNLOCKED");
    }

    @Test
    void graphForUnknownRoadmapReturns404() throws Exception {
        mockMvc.perform(get("/api/roadmaps/" + java.util.UUID.randomUUID() + "/graph"))
                .andExpect(status().isNotFound());
    }
}
