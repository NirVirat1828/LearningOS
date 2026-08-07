package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningos.backend.dto.UpdateProgressRequest;
import com.learningos.backend.entity.ProgressStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies completing a topic's progress stamps today's date (making it
 * show up on the learning calendar), and that moving off COMPLETED clears
 * it again -- the same pattern as Task's completedDate.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProgressApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completingATopicStampsTodayAsCompletedDate() throws Exception {
        String topicId = findTopicIdByTitle("REST Controllers");

        UpdateProgressRequest request = new UpdateProgressRequest(ProgressStatus.COMPLETED, 100);
        mockMvc.perform(patch("/api/topics/" + topicId + "/progress")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedDate").value(LocalDate.now().toString()));

        // Reverting away from COMPLETED clears the date again.
        UpdateProgressRequest revert = new UpdateProgressRequest(ProgressStatus.IN_PROGRESS, 50);
        mockMvc.perform(patch("/api/topics/" + topicId + "/progress")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(revert)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completedDate").doesNotExist());
    }

    @Test
    void completingATopicMakesItAppearOnTheCalendar() throws Exception {
        String topicId = findTopicIdByTitle("REST Controllers");
        UpdateProgressRequest request = new UpdateProgressRequest(ProgressStatus.COMPLETED, 100);
        mockMvc.perform(patch("/api/topics/" + topicId + "/progress")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/calendar/days/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topicsFinished[*].title",
                        org.hamcrest.Matchers.hasItem("REST Controllers")));
    }

    @Test
    void updatingProgressForUnknownTopicReturns404() throws Exception {
        UpdateProgressRequest request = new UpdateProgressRequest(ProgressStatus.COMPLETED, 100);
        mockMvc.perform(patch("/api/topics/" + java.util.UUID.randomUUID() + "/progress")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private String findTopicIdByTitle(String title) throws Exception {
        String roadmapsJson = mockMvc.perform(get("/api/roadmaps"))
                .andReturn().getResponse().getContentAsString();
        JsonNode roadmap = objectMapper.readTree(roadmapsJson).get("data").get(0);
        for (JsonNode course : roadmap.get("courses")) {
            for (JsonNode module : course.get("modules")) {
                for (JsonNode topic : module.get("topics")) {
                    if (topic.get("title").asText().equals(title)) {
                        return topic.get("id").asText();
                    }
                }
            }
        }
        throw new IllegalStateException("Topic not found in seeded data: " + title);
    }
}
