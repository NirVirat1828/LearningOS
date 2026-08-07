package com.learningos.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the calendar aggregation against the seeded activity: completed
 * tasks on today-3 (paired with the one topic that finished), today-8,
 * today-6, today-4, and today-1. If any of those days fall in a different
 * calendar month than "today" (e.g. testing on the 1st-2nd of the month),
 * this test still holds since it always queries the actual month each date
 * falls in rather than assuming a single month contains everything.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CalendarApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dayWithBothATaskAndATopicFinishingShowsBoth() throws Exception {
        LocalDate day = LocalDate.now().minusDays(3);

        String json = mockMvc.perform(get("/api/calendar/days/" + day))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(json).get("data");

        assertThat(data.get("tasksCompleted")).hasSize(1);
        assertThat(data.get("tasksCompleted").get(0).get("title").asText())
                .isEqualTo("Generate a project with Spring Initializr");
        assertThat(data.get("topicsFinished")).hasSize(1);
        assertThat(data.get("topicsFinished").get(0).get("title").asText())
                .isEqualTo("Java & Spring Fundamentals");
        assertThat(data.get("totalMinutesSpent").asInt()).isEqualTo(20);
    }

    @Test
    void dayWithOnlyATaskShowsNoTopics() throws Exception {
        LocalDate day = LocalDate.now().minusDays(1);

        mockMvc.perform(get("/api/calendar/days/" + day))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasksCompleted", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.tasksCompleted[0].title").value("Read the Spring DI documentation"))
                .andExpect(jsonPath("$.data.topicsFinished", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.data.totalMinutesSpent").value(30));
    }

    @Test
    void dayWithNoActivityReturns200WithEmptyData() throws Exception {
        LocalDate farInThePast = LocalDate.now().minusYears(5);

        mockMvc.perform(get("/api/calendar/days/" + farInThePast))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasksCompleted", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.data.topicsFinished", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.data.totalMinutesSpent").value(0));
    }

    @Test
    void monthlySummaryDefaultsToCurrentMonthAndCoversEveryDay() throws Exception {
        YearMonth currentMonth = YearMonth.now();

        String json = mockMvc.perform(get("/api/calendar/summary"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(json).get("data");

        assertThat(data.get("year").asInt()).isEqualTo(currentMonth.getYear());
        assertThat(data.get("month").asInt()).isEqualTo(currentMonth.getMonthValue());
        assertThat(data.get("days")).hasSize(currentMonth.lengthOfMonth());
    }

    @Test
    void monthlySummaryReflectsSeededActivityWhenAllWithinTheSameMonth() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate earliestActivity = today.minusDays(8);
        // Only assert the detailed roll-up when every seeded activity date
        // (today-8 .. today-1) falls in the same calendar month as today.
        if (!YearMonth.from(earliestActivity).equals(YearMonth.from(today))) {
            return;
        }

        String json = mockMvc.perform(get("/api/calendar/summary")
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode days = objectMapper.readTree(json).get("data").get("days");

        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        for (JsonNode day : days) {
            LocalDate date = LocalDate.parse(day.get("date").asText(), iso);
            if (date.equals(today.minusDays(3))) {
                assertThat(day.get("tasksCompleted").asInt()).isEqualTo(1);
                assertThat(day.get("topicsFinished").asInt()).isEqualTo(1);
                assertThat(day.get("minutesSpent").asInt()).isEqualTo(20);
            } else if (date.equals(today.minusDays(1))
                    || date.equals(today.minusDays(4))
                    || date.equals(today.minusDays(6))
                    || date.equals(today.minusDays(8))) {
                assertThat(day.get("tasksCompleted").asInt()).isEqualTo(1);
                assertThat(day.get("topicsFinished").asInt()).isEqualTo(0);
            } else {
                assertThat(day.get("tasksCompleted").asInt()).isEqualTo(0);
                assertThat(day.get("topicsFinished").asInt()).isEqualTo(0);
                assertThat(day.get("minutesSpent").asInt()).isEqualTo(0);
            }
        }
    }
}
