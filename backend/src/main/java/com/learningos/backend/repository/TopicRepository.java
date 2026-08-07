package com.learningos.backend.repository;

import com.learningos.backend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByModuleId(UUID moduleId);
}
