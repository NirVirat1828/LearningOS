package com.learningos.backend.dto;

import java.util.List;

public record MonthlyCalendarResponse(
        int year,
        int month,
        List<DaySummary> days
) {
}
