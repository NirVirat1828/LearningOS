package com.learningos.backend.seed;

import com.learningos.backend.entity.Course;
import com.learningos.backend.entity.Module;
import com.learningos.backend.entity.Roadmap;
import com.learningos.backend.entity.Topic;
import com.learningos.backend.entity.TopicDependency;
import com.learningos.backend.repository.RoadmapRepository;
import com.learningos.backend.repository.TopicDependencyRepository;
import com.learningos.backend.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that DataSeeder ran on context startup and that the seeded
 * relationships (cascade saves, back-references, dependency edges) hold.
 */
@SpringBootTest
@Transactional
class DataSeederTest {

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicDependencyRepository topicDependencyRepository;

    @Test
    void seedsExactlyOneRoadmapWithFullHierarchy() {
        List<Roadmap> roadmaps = roadmapRepository.findAll();
        assertThat(roadmaps).hasSize(1);

        Roadmap roadmap = roadmaps.get(0);
        assertThat(roadmap.getId()).isNotNull();
        assertThat(roadmap.getCourses()).hasSize(2);

        Course backendCourse = roadmap.getCourses().stream()
                .filter(c -> c.getTitle().contains("Backend"))
                .findFirst()
                .orElseThrow();
        assertThat(backendCourse.getRoadmap()).isEqualTo(roadmap);
        assertThat(backendCourse.getModules()).hasSize(2);

        Module springBasicsModule = backendCourse.getModules().stream()
                .filter(m -> m.getTitle().contains("Spring Boot Basics"))
                .findFirst()
                .orElseThrow();
        assertThat(springBasicsModule.getCourse()).isEqualTo(backendCourse);
        assertThat(springBasicsModule.getTopics()).hasSize(3);

        Topic dependencyInjectionTopic = springBasicsModule.getTopics().stream()
                .filter(t -> t.getTitle().equals("Dependency Injection"))
                .findFirst()
                .orElseThrow();
        assertThat(dependencyInjectionTopic.getModule()).isEqualTo(springBasicsModule);
        assertThat(dependencyInjectionTopic.getTasks()).hasSize(3);
        assertThat(dependencyInjectionTopic.getResources()).hasSize(5);
        assertThat(dependencyInjectionTopic.getProgress()).isNotNull();
        assertThat(dependencyInjectionTopic.getProgress().getTopic()).isEqualTo(dependencyInjectionTopic);
    }

    @Test
    void topicDependenciesFormThePrerequisiteChain() {
        List<TopicDependency> dependencies = topicDependencyRepository.findAll();
        assertThat(dependencies).hasSize(3);

        Topic springFundamentals = findTopicByTitle("Java & Spring Fundamentals");
        Topic restControllers = findTopicByTitle("REST Controllers");
        Topic dependencyInjection = findTopicByTitle("Dependency Injection");
        Topic entityMapping = findTopicByTitle("Entity Mapping & Relationships");

        List<TopicDependency> dependencyInjectionPrereqs =
                topicDependencyRepository.findByTopicId(dependencyInjection.getId());
        assertThat(dependencyInjectionPrereqs).hasSize(1);
        assertThat(dependencyInjectionPrereqs.get(0).getDependsOnTopic()).isEqualTo(springFundamentals);

        List<TopicDependency> restControllersPrereqs =
                topicDependencyRepository.findByTopicId(restControllers.getId());
        assertThat(restControllersPrereqs).hasSize(1);
        assertThat(restControllersPrereqs.get(0).getDependsOnTopic()).isEqualTo(dependencyInjection);

        List<TopicDependency> dependencyInjectionDependents =
                topicDependencyRepository.findByDependsOnTopicId(dependencyInjection.getId());
        assertThat(dependencyInjectionDependents).hasSize(1);
        assertThat(dependencyInjectionDependents.get(0).getTopic()).isEqualTo(restControllers);

        List<TopicDependency> entityMappingPrereqs =
                topicDependencyRepository.findByTopicId(entityMapping.getId());
        assertThat(entityMappingPrereqs).hasSize(1);
        assertThat(entityMappingPrereqs.get(0).getDependsOnTopic()).isEqualTo(restControllers);
    }

    private Topic findTopicByTitle(String title) {
        return topicRepository.findAll().stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst()
                .orElseThrow();
    }
}
