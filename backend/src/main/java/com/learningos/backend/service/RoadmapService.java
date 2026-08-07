package com.learningos.backend.service;

import com.learningos.backend.dto.RoadmapRequest;
import com.learningos.backend.dto.RoadmapResponse;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.exception.ResourceNotFoundException;
import com.learningos.backend.mapper.SyllabusMapper;
import com.learningos.backend.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;

    public List<RoadmapResponse> getAllRoadmaps() {
        return roadmapRepository.findAll().stream().map(SyllabusMapper::toResponse).toList();
    }

    public RoadmapResponse getRoadmap(UUID id) {
        return SyllabusMapper.toResponse(findRoadmapOrThrow(id));
    }

    @Transactional
    public RoadmapResponse createRoadmap(RoadmapRequest request) {
        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(request.title());
        roadmap.setDescription(request.description());
        return SyllabusMapper.toResponse(roadmapRepository.save(roadmap));
    }

    @Transactional
    public RoadmapResponse updateRoadmap(UUID id, RoadmapRequest request) {
        Roadmap roadmap = findRoadmapOrThrow(id);
        roadmap.setTitle(request.title());
        roadmap.setDescription(request.description());
        return SyllabusMapper.toResponse(roadmap);
    }

    @Transactional
    public void deleteRoadmap(UUID id) {
        roadmapRepository.delete(findRoadmapOrThrow(id));
    }

    private Roadmap findRoadmapOrThrow(UUID id) {
        return roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + id));
    }
}
