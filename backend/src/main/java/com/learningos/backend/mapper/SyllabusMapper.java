package com.learningos.backend.mapper;

import com.learningos.backend.dto.CourseResponse;
import com.learningos.backend.dto.ModuleResponse;
import com.learningos.backend.dto.RoadmapResponse;
import com.learningos.backend.dto.TopicResponse;
import com.learningos.backend.entity.Course;
import com.learningos.backend.entity.Module;
import com.learningos.backend.entity.Progress;
import com.learningos.backend.entity.ProgressStatus;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.entity.Topic;

/**
 * Converts the entity graph into the nested response DTOs the syllabus API
 * returns (Roadmap -> Courses -> Modules -> Topics). Kept as one static
 * utility, rather than split per-entity, since each level's mapping calls
 * straight into the next.
 */
public final class SyllabusMapper {

    private SyllabusMapper() {
    }

    public static RoadmapResponse toResponse(Roadmap roadmap) {
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                roadmap.getCourses().stream().map(SyllabusMapper::toResponse).toList(),
                roadmap.getCreatedAt(),
                roadmap.getUpdatedAt()
        );
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getRoadmap().getId(),
                course.getTitle(),
                course.getDescription(),
                course.getModules().stream().map(SyllabusMapper::toResponse).toList(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    public static ModuleResponse toResponse(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getCourse().getId(),
                module.getTitle(),
                module.getDescription(),
                module.getTopics().stream().map(SyllabusMapper::toResponse).toList(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }

    public static TopicResponse toResponse(Topic topic) {
        Progress progress = topic.getProgress();
        return new TopicResponse(
                topic.getId(),
                topic.getModule().getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getDifficulty(),
                topic.getEstimatedMinutes(),
                progress != null ? progress.getStatus() : ProgressStatus.NOT_STARTED,
                progress != null ? progress.getCompletionPercentage() : 0,
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }
}
