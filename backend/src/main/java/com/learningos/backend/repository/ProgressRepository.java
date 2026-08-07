package com.learningos.backend.repository;

import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    Optional<Progress> findByTopicId(UUID topicId);

    List<Progress> findByStatusAndCompletedDate(ProgressStatus status, LocalDate completedDate);

    List<Progress> findByStatusAndCompletedDateBetween(ProgressStatus status, LocalDate start, LocalDate end);
}
