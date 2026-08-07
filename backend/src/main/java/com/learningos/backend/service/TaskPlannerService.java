package com.learningos.backend.service;

import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.mapper.TaskMapper;
import com.learningos.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Builds "today's plan" from the raw task pool. Two steps, run every time
 * this is called — see the class-level explanation in each step below for
 * why that's safe to do repeatedly rather than only once per day.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskPlannerService {

    private final TaskRepository taskRepository;

    @Value("${learningos.planner.daily-capacity:5}")
    private int dailyCapacity;

    @Transactional
    public List<TaskResponse> generateTodaysTasks() {
        LocalDate today = LocalDate.now();

        // 1. Carry forward: a PENDING task left over from a prior day moves to
        // today instead of quietly staying lost in the past. COMPLETED and
        // SKIPPED tasks are left alone — they're resolved, carrying them
        // forward would just resurface closed work.
        List<Task> overdue = taskRepository.findByStatusAndScheduledDateBefore(TaskStatus.PENDING, today);
        overdue.forEach(task -> task.setScheduledDate(today));

        // 2. Top the day back up to capacity from the prioritized backlog.
        // Safe to call this on a day that's already partially planned: it
        // only ever pulls in enough extra tasks to reach the cap, so calling
        // it again after completing a task naturally pulls in the next
        // highest-priority backlog item rather than leaving a gap.
        long scheduledToday = taskRepository.countByScheduledDateAndStatus(today, TaskStatus.PENDING);
        int remainingCapacity = (int) Math.max(0, dailyCapacity - scheduledToday);
        if (remainingCapacity > 0) {
            taskRepository.findByScheduledDateIsNullAndStatus(TaskStatus.PENDING).stream()
                    .sorted(Comparator.comparing(Task::getPriority).thenComparing(Task::getCreatedAt))
                    .limit(remainingCapacity)
                    .forEach(task -> task.setScheduledDate(today));
        }

        return taskRepository.findByScheduledDate(today).stream()
                .map(TaskMapper::toResponse)
                .toList();
    }
}
