package com.learningos.backend.dto;

import java.time.LocalDate;

public record DaySummary(
        LocalDate date,
        int tasksCompleted,
        int topicsFinished,
        int minutesSpent
) {
}
