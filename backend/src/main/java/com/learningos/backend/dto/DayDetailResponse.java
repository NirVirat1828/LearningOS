package com.learningos.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record DayDetailResponse(
        LocalDate date,
        List<TaskResponse> tasksCompleted,
        List<TopicResponse> topicsFinished,
        int totalMinutesSpent
) {
}
