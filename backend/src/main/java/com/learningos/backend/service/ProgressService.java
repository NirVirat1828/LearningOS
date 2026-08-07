package com.learningos.backend.service;

import com.learningos.backend.dto.ProgressResponse;
import com.learningos.backend.dto.UpdateProgressRequest;
import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.ProgressMapper;
import com.learningos.backend.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    private final ProgressRepository progressRepository;

    public ProgressResponse getProgressForTopic(UUID topicId) {
        return ProgressMapper.toResponse(findProgressOrThrow(topicId));
    }

    /**
     * completedDate is set the moment status becomes COMPLETED and cleared
     * on any other transition — this is what lets the learning calendar
     * know which day a topic actually finished on.
     */
    @Transactional
    public ProgressResponse updateProgress(UUID topicId, UpdateProgressRequest request) {
        Progress progress = findProgressOrThrow(topicId);
        progress.setStatus(request.status());
        progress.setCompletionPercentage(request.completionPercentage());
        progress.setCompletedDate(request.status() == ProgressStatus.COMPLETED ? LocalDate.now() : null);
        return ProgressMapper.toResponse(progress);
    }

    private Progress findProgressOrThrow(UUID topicId) {
        return progressRepository.findByTopicId(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for topic: " + topicId));
    }
}
