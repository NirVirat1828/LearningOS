package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningos.backend.dto.CreateDependencyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises TopicDependency creation against the seeded prerequisite chain
 * (Java & Spring Fundamentals -> Dependency Injection -> REST Controllers ->
 * Entity Mapping & Relationships), including both cycle-rejection cases:
 * self-dependency, and a multi-hop cycle closing back through the chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TopicDependencyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String springFundamentalsId;
    private String dependencyInjectionId;
    private String restControllersId;
    private String componentsId;

    @BeforeEach
    void loadSeededTopicIds() throws Exception {
        String roadmapsJson = mockMvc.perform(get("/api/roadmaps"))
                .andReturn().getResponse().getContentAsString();
        JsonNode roadmap = objectMapper.readTree(roadmapsJson).get("data").get(0);
        JsonNode allTopics = flattenTopics(roadmap);

        springFundamentalsId = titled(allTopics, "Java & Spring Fundamentals");
        dependencyInjectionId = titled(allTopics, "Dependency Injection");
        restControllersId = titled(allTopics, "REST Controllers");
        componentsId = titled(allTopics, "Components & Props");
    }

    @Test
    void creatingAValidDependencySucceeds() throws Exception {
        CreateDependencyRequest request = new CreateDependencyRequest(UUID.fromString(dependencyInjectionId));
        mockMvc.perform(post("/api/topics/" + componentsId + "/dependencies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.topicId").value(componentsId))
                .andExpect(jsonPath("$.data.dependsOnTopicId").value(dependencyInjectionId));

        mockMvc.perform(get("/api/topics/" + componentsId + "/dependencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void selfDependencyIsRejected() throws Exception {
        CreateDependencyRequest request = new CreateDependencyRequest(UUID.fromString(dependencyInjectionId));
        mockMvc.perform(post("/api/topics/" + dependencyInjectionId + "/dependencies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("itself")));
    }

    @Test
    void closingACycleAcrossTheExistingChainIsRejected() throws Exception {
        // Existing chain: Java & Spring Fundamentals -> Dependency Injection -> REST Controllers.
        // Making Fundamentals require REST Controllers would close that loop.
        CreateDependencyRequest request = new CreateDependencyRequest(UUID.fromString(restControllersId));
        mockMvc.perform(post("/api/topics/" + springFundamentalsId + "/dependencies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("circular")));
    }

    @Test
    void deletingADependencyRemovesIt() throws Exception {
        CreateDependencyRequest request = new CreateDependencyRequest(UUID.fromString(springFundamentalsId));
        String created = mockMvc.perform(post("/api/topics/" + componentsId + "/dependencies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String dependencyId = objectMapper.readTree(created).get("data").get("id").asText();

        mockMvc.perform(delete("/api/dependencies/" + dependencyId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/topics/" + componentsId + "/dependencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(0)));
    }

    private JsonNode flattenTopics(JsonNode roadmap) {
        com.fasterxml.jackson.databind.node.ArrayNode topics = objectMapper.createArrayNode();
        roadmap.get("courses").forEach(course ->
                course.get("modules").forEach(module ->
                        module.get("topics").forEach(topics::add)));
        return topics;
    }

    private String titled(JsonNode topics, String title) {
        return StreamSupport.stream(topics.spliterator(), false)
                .filter(t -> t.get("title").asText().equals(title))
                .findFirst()
                .orElseThrow()
                .get("id").asText();
    }
}
