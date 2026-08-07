package com.learningos.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A single actionable to-do item that belongs to exactly one Topic, and
 * doubles as the unit the daily planner schedules. {@code scheduledDate}
 * null means the task sits in the backlog (not yet placed on any day's
 * plan); a non-null value means it's on that day's plan.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    private LocalDate scheduledDate;

    /** Set when status transitions to COMPLETED, cleared otherwise. Drives the learning calendar. */
    private LocalDate completedDate;

    /** How long the task actually took, logged alongside completion. Null if never recorded. */
    private Integer timeSpentMinutes;
}
