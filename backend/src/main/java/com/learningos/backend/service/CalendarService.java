package com.learningos.backend.service;

import com.learningos.backend.dto.DayDetailResponse;
import com.learningos.backend.dto.DaySummary;
import com.learningos.backend.dto.MonthlyCalendarResponse;
import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.mapper.SyllabusMapper;
import com.learningos.backend.mapper.TaskMapper;
import com.learningos.backend.repository.ProgressRepository;
import com.learningos.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only aggregation over Task/Progress completion dates -- there's no
 * "Calendar" entity, this just reshapes existing data for the contribution
 * calendar and its day drill-down.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final TaskRepository taskRepository;
    private final ProgressRepository progressRepository;

    public MonthlyCalendarResponse getMonthlySummary(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Task> completedTasks =
                taskRepository.findByStatusAndCompletedDateBetween(TaskStatus.COMPLETED, start, end);
        List<Progress> finishedTopics =
                progressRepository.findByStatusAndCompletedDateBetween(ProgressStatus.COMPLETED, start, end);

        Map<LocalDate, List<Task>> tasksByDate = completedTasks.stream()
                .collect(Collectors.groupingBy(Task::getCompletedDate));
        Map<LocalDate, List<Progress>> topicsByDate = finishedTopics.stream()
                .collect(Collectors.groupingBy(Progress::getCompletedDate));

        List<DaySummary> days = start.datesUntil(end.plusDays(1))
                .map(date -> {
                    List<Task> dayTasks = tasksByDate.getOrDefault(date, List.of());
                    int minutesSpent = dayTasks.stream()
                            .mapToInt(t -> t.getTimeSpentMinutes() != null ? t.getTimeSpentMinutes() : 0)
                            .sum();
                    return new DaySummary(
                            date,
                            dayTasks.size(),
                            topicsByDate.getOrDefault(date, List.of()).size(),
                            minutesSpent
                    );
                })
                .toList();

        return new MonthlyCalendarResponse(year, month, days);
    }

    public DayDetailResponse getDayDetail(LocalDate date) {
        List<Task> tasks = taskRepository.findByStatusAndCompletedDate(TaskStatus.COMPLETED, date);
        List<Progress> topics = progressRepository.findByStatusAndCompletedDate(ProgressStatus.COMPLETED, date);

        int totalMinutes = tasks.stream()
                .mapToInt(t -> t.getTimeSpentMinutes() != null ? t.getTimeSpentMinutes() : 0)
                .sum();

        return new DayDetailResponse(
                date,
                tasks.stream().map(TaskMapper::toResponse).toList(),
                topics.stream().map(progress -> SyllabusMapper.toResponse(progress.getTopic())).toList(),
                totalMinutes
        );
    }
}
