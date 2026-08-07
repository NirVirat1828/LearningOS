package com.learningos.backend.entity;

/**
 * Declaration order matters: the planner sorts backlog tasks with
 * {@code Comparator.comparing(Task::getPriority)}, which uses each
 * constant's ordinal (declaration position), not the stored STRING value —
 * so HIGH must stay first.
 */
public enum Priority {
    HIGH,
    MEDIUM,
    LOW
}
