package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningos.backend.dto.ResourceRequest;
import com.learningos.backend.entity.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises Resource CRUD against the seeded Dependency Injection topic,
 * which carries one resource of every type (OFFICIAL_DOCS, YOUTUBE, BOOK,
 * GITHUB, ARTICLE) specifically so this can assert the full spread exists.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResourceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dependencyInjectionTopicHasOneResourceOfEachType() throws Exception {
        String topicId = findDependencyInjectionTopicId();

        String resourcesJson = mockMvc.perform(get("/api/topics/" + topicId + "/resources"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode resources = objectMapper.readTree(resourcesJson).get("data");
        assertThat(resources).hasSize(5);

        List<String> types = StreamSupport.stream(resources.spliterator(), false)
                .map(r -> r.get("type").asText())
                .toList();
        assertThat(types).containsExactlyInAnyOrder("OFFICIAL_DOCS", "YOUTUBE", "BOOK", "GITHUB", "ARTICLE");
    }

    @Test
    void fullCrudRoundTrip() throws Exception {
        String topicId = findDependencyInjectionTopicId();

        ResourceRequest createRequest = new ResourceRequest(
                "Effective Java", "https://www.oreilly.com/library/view/effective-java/9780134686097/", ResourceType.BOOK);
        String createdJson = mockMvc.perform(post("/api/topics/" + topicId + "/resources")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Effective Java"))
                .andExpect(jsonPath("$.data.type").value("BOOK"))
                .andReturn().getResponse().getContentAsString();
        String resourceId = objectMapper.readTree(createdJson).get("data").get("id").asText();

        mockMvc.perform(get("/api/resources/" + resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Effective Java"));

        ResourceRequest updateRequest = new ResourceRequest(
                "Effective Java (3rd Edition)", "https://www.oreilly.com/library/view/effective-java/9780134686097/",
                ResourceType.BOOK);
        mockMvc.perform(put("/api/resources/" + resourceId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Effective Java (3rd Edition)"));

        mockMvc.perform(delete("/api/resources/" + resourceId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/resources/" + resourceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void creatingResourceWithBlankTitleReturns400() throws Exception {
        String topicId = findDependencyInjectionTopicId();
        String invalid = "{\"title\":\"\",\"url\":null,\"type\":\"ARTICLE\"}";
        mockMvc.perform(post("/api/topics/" + topicId + "/resources")
                        .contentType("application/json")
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatingResourceUnderUnknownTopicReturns404() throws Exception {
        String request = "{\"title\":\"Orphan Resource\",\"url\":null,\"type\":\"ARTICLE\"}";
        mockMvc.perform(post("/api/topics/" + java.util.UUID.randomUUID() + "/resources")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isNotFound());
    }

    private String findDependencyInjectionTopicId() throws Exception {
        String roadmapsJson = mockMvc.perform(get("/api/roadmaps"))
                .andReturn().getResponse().getContentAsString();
        JsonNode roadmap = objectMapper.readTree(roadmapsJson).get("data").get(0);
        for (JsonNode course : roadmap.get("courses")) {
            for (JsonNode module : course.get("modules")) {
                for (JsonNode topic : module.get("topics")) {
                    if (topic.get("title").asText().equals("Dependency Injection")) {
                        return topic.get("id").asText();
                    }
                }
            }
        }
        throw new IllegalStateException("Dependency Injection topic not found in seeded data");
    }
}
