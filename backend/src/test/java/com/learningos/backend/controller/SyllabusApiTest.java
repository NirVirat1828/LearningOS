package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningos.backend.dto.CourseRequest;
import com.learningos.backend.dto.RoadmapRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the syllabus CRUD API end to end against the seeded H2 database:
 * nested GET shape, create/update/delete round trips, and validation errors.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SyllabusApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRoadmapsReturnsFullyNestedTree() throws Exception {
        mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Full-Stack Web Development"))
                .andExpect(jsonPath("$.data[0].courses", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data[0].courses[0].modules[0].topics[0].title").exists())
                .andExpect(jsonPath("$.data[0].courses[0].modules[0].topics[0].difficulty").exists())
                .andExpect(jsonPath("$.data[0].courses[0].modules[0].topics[0].estimatedMinutes").exists())
                .andExpect(jsonPath("$.data[0].courses[0].modules[0].topics[0].completionStatus").exists());
    }

    @Test
    void fullCrudRoundTripAcrossAllFourLevels() throws Exception {
        RoadmapRequest roadmapRequest = new RoadmapRequest("Data Engineering", "Pipelines and warehousing");
        String roadmapJson = mockMvc.perform(post("/api/roadmaps")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(roadmapRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.title").value("Data Engineering"))
                .andExpect(jsonPath("$.data.courses", org.hamcrest.Matchers.hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        String roadmapId = objectMapper.readTree(roadmapJson).get("data").get("id").asText();

        CourseRequest courseRequest = new CourseRequest("SQL Fundamentals", null);
        String courseJson = mockMvc.perform(post("/api/roadmaps/" + roadmapId + "/courses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roadmapId").value(roadmapId))
                .andReturn().getResponse().getContentAsString();
        String courseId = objectMapper.readTree(courseJson).get("data").get("id").asText();

        // Update the course
        CourseRequest renamed = new CourseRequest("SQL Fundamentals (Updated)", "Now with window functions");
        mockMvc.perform(put("/api/courses/" + courseId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(renamed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("SQL Fundamentals (Updated)"));

        // Roadmap's nested read now reflects the new course
        mockMvc.perform(get("/api/roadmaps/" + roadmapId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.courses[0].title").value("SQL Fundamentals (Updated)"));

        // Delete cleans up (course has no modules/topics, so no FK conflict)
        mockMvc.perform(delete("/api/courses/" + courseId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/courses/" + courseId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/roadmaps/" + roadmapId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/roadmaps/" + roadmapId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRoadmapWithBlankTitleReturns400() throws Exception {
        RoadmapRequest invalid = new RoadmapRequest("", "no title");
        mockMvc.perform(post("/api/roadmaps")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("title")));
    }

    @Test
    void getUnknownRoadmapReturns404() throws Exception {
        mockMvc.perform(get("/api/roadmaps/" + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createCourseUnderUnknownRoadmapReturns404() throws Exception {
        CourseRequest request = new CourseRequest("Orphan Course", null);
        mockMvc.perform(post("/api/roadmaps/" + java.util.UUID.randomUUID() + "/courses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void estimatedMinutesMustNotBeNegative() throws Exception {
        String moduleId = mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String firstModuleId = objectMapper.readTree(moduleId)
                .get("data").get(0).get("courses").get(0).get("modules").get(0).get("id").asText();

        String badTopic = """
                {"title":"Bad Topic","description":null,"difficulty":"BEGINNER","estimatedMinutes":-5}
                """;
        mockMvc.perform(post("/api/modules/" + firstModuleId + "/topics")
                        .contentType("application/json")
                        .content(badTopic))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("estimatedMinutes")));
    }
}
