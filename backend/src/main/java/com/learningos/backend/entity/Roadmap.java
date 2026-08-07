package com.learningos.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Root of the learning hierarchy: a Roadmap owns a list of Courses.
 * Deleting a Roadmap cascades down through Course -> Module -> Topic and
 * everything a Topic owns (tasks, resources, progress).
 */
@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
public class Roadmap extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        courses.add(course);
        course.setRoadmap(this);
    }
}
