package com.learningos.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Tracks completion state for exactly one Topic. Owns the foreign key
 * (unique {@code topic_id}), making this the owning side of the one-to-one
 * relationship; Topic's {@code progress} field is the inverse/mappedBy side.
 */
@Entity
@Table(name = "progress")
@Getter
@Setter
@NoArgsConstructor
public class Progress extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(nullable = false)
    private int completionPercentage = 0;

    /** Set when status transitions to COMPLETED, cleared otherwise. Drives the learning calendar. */
    private LocalDate completedDate;
}
