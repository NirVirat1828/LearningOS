package com.learningos.backend.repository;

import com.learningos.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByRoadmapId(UUID roadmapId);
}
