package com.learningos.backend.repository;

import com.learningos.backend.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
}
