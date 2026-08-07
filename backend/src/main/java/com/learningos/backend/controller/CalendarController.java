package com.learningos.backend.controller;

import com.learningos.backend.dto.ApiResponse;
import com.learningos.backend.dto.DayDetailResponse;
import com.learningos.backend.dto.MonthlyCalendarResponse;
import com.learningos.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /** year/month default to the current month so the frontend's initial load needs no params. */
    @GetMapping("/summary")
    public ApiResponse<MonthlyCalendarResponse> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth target = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        return ApiResponse.success(calendarService.getMonthlySummary(target.getYear(), target.getMonthValue()));
    }

    /** Every valid date is a legitimate query, even with zero activity -- always 200, never 404. */
    @GetMapping("/days/{date}")
    public ApiResponse<DayDetailResponse> getDayDetail(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(calendarService.getDayDetail(date));
    }
}
