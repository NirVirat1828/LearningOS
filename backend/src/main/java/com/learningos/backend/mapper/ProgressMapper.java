package com.learningos.backend.mapper;

import com.learningos.backend.dto.ProgressResponse;
import com.learningos.backend.entity.Progress;

public final class ProgressMapper {

    private ProgressMapper() {
    }

    public static ProgressResponse toResponse(Progress progress) {
        return new ProgressResponse(
                progress.getId(),
                progress.getTopic().getId(),
                progress.getTopic().getTitle(),
                progress.getStatus(),
                progress.getCompletionPercentage(),
                progress.getCompletedDate(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }
}
