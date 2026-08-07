package com.learningos.backend.service;

import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.dto.UpdateTaskStatusRequest;
import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.TaskMapper;
import com.learningos.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskResponse> getTodaysTasks() {
        return taskRepository.findByScheduledDate(LocalDate.now()).stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    /**
     * Only PENDING, unscheduled tasks are "backlog" in the actionable sense
     * — a task can't be COMPLETED or SKIPPED without having gone through a
     * day's plan first. Sorted in Java (not via a derived-query ORDER BY)
     * because {@code priority} is stored as a STRING column: SQL would sort
     * it alphabetically ("HIGH" &lt; "LOW" &lt; "MEDIUM"), not by severity.
     * Comparing the enum itself in Java uses its ordinal instead.
     */
    public List<TaskResponse> getBacklog() {
        return taskRepository.findByScheduledDateIsNullAndStatus(TaskStatus.PENDING).stream()
                .sorted(Comparator.comparing(Task::getPriority).thenComparing(Task::getCreatedAt))
                .map(TaskMapper::toResponse)
                .toList();
    }

    /**
     * completedDate/timeSpentMinutes only make sense alongside COMPLETED:
     * set completedDate to today (and timeSpentMinutes if provided) on the
     * transition in; clear both on any transition away, so a task that's no
     * longer COMPLETED doesn't keep claiming a completion date.
     */
    @Transactional
    public TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        task.setStatus(request.status());
        if (request.status() == TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDate.now());
            if (request.timeSpentMinutes() != null) {
                task.setTimeSpentMinutes(request.timeSpentMinutes());
            }
        } else {
            task.setCompletedDate(null);
            task.setTimeSpentMinutes(null);
        }
        return TaskMapper.toResponse(task);
    }
}
