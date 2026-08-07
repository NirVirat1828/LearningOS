package com.learningos.backend.repository;

import com.learningos.backend.entity.Task;
import com.learningos.backend.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByTopicId(UUID topicId);

    List<Task> findByScheduledDate(LocalDate scheduledDate);

    List<Task> findByStatusAndScheduledDateBefore(TaskStatus status, LocalDate date);

    List<Task> findByScheduledDateIsNullAndStatus(TaskStatus status);

    long countByScheduledDateAndStatus(LocalDate scheduledDate, TaskStatus status);

    List<Task> findByStatusAndCompletedDate(TaskStatus status, LocalDate completedDate);

    List<Task> findByStatusAndCompletedDateBetween(TaskStatus status, LocalDate start, LocalDate end);
}
