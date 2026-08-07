package com.learningos.backend.service;

import com.learningos.backend.dto.ModuleRequest;
import com.learningos.backend.dto.ModuleResponse;
import com.learningos.backend.entity.Course;
import com.learningos.backend.entity.Module;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.SyllabusMapper;
import com.learningos.backend.repository.CourseRepository;
import com.learningos.backend.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public List<ModuleResponse> getModulesByCourse(UUID courseId) {
        ensureCourseExists(courseId);
        return moduleRepository.findByCourseId(courseId).stream().map(SyllabusMapper::toResponse).toList();
    }

    public ModuleResponse getModule(UUID id) {
        return SyllabusMapper.toResponse(findModuleOrThrow(id));
    }

    @Transactional
    public ModuleResponse createModule(UUID courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        Module module = new Module();
        module.setTitle(request.title());
        module.setDescription(request.description());
        course.addModule(module);
        return SyllabusMapper.toResponse(moduleRepository.save(module));
    }

    @Transactional
    public ModuleResponse updateModule(UUID id, ModuleRequest request) {
        Module module = findModuleOrThrow(id);
        module.setTitle(request.title());
        module.setDescription(request.description());
        return SyllabusMapper.toResponse(module);
    }

    @Transactional
    public void deleteModule(UUID id) {
        moduleRepository.delete(findModuleOrThrow(id));
    }

    private void ensureCourseExists(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
    }

    private Module findModuleOrThrow(UUID id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));
    }
}
