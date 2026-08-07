package com.learningos.backend.repository;

import com.learningos.backend.entity.TopicDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicDependencyRepository extends JpaRepository<TopicDependency, UUID> {

    List<TopicDependency> findByTopicId(UUID topicId);

    List<TopicDependency> findByDependsOnTopicId(UUID dependsOnTopicId);
}
