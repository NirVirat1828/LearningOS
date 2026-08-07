package com.learningos.backend.mapper;

import com.learningos.backend.dto.TaskResponse;
import com.learningos.backend.entity.Task;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTopic().getId(),
                task.getTopic().getTitle(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getScheduledDate(),
                task.getCompletedDate(),
                task.getTimeSpentMinutes(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
