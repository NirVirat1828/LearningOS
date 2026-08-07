package com.learningos.backend.service;

import com.learningos.backend.dto.CourseRequest;
import com.learningos.backend.dto.CourseResponse;
import com.learningos.backend.entity.Course;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.SyllabusMapper;
import com.learningos.backend.repository.CourseRepository;
import com.learningos.backend.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final RoadmapRepository roadmapRepository;

    public List<CourseResponse> getCoursesByRoadmap(UUID roadmapId) {
        ensureRoadmapExists(roadmapId);
        return courseRepository.findByRoadmapId(roadmapId).stream().map(SyllabusMapper::toResponse).toList();
    }

    public CourseResponse getCourse(UUID id) {
        return SyllabusMapper.toResponse(findCourseOrThrow(id));
    }

    @Transactional
    public CourseResponse createCourse(UUID roadmapId, CourseRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        roadmap.addCourse(course);
        return SyllabusMapper.toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        Course course = findCourseOrThrow(id);
        course.setTitle(request.title());
        course.setDescription(request.description());
        return SyllabusMapper.toResponse(course);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        courseRepository.delete(findCourseOrThrow(id));
    }

    private void ensureRoadmapExists(UUID roadmapId) {
        if (!roadmapRepository.existsById(roadmapId)) {
            throw new ResourceNotFoundException("Roadmap not found: " + roadmapId);
        }
    }

    private Course findCourseOrThrow(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }
}
