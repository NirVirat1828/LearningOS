package com.learningos.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A Topic belongs to exactly one Module and owns Tasks, Resources, and a
 * Progress record. Topics can also depend on other Topics as prerequisites,
 * modeled through the {@link TopicDependency} join entity rather than a
 * plain @ManyToMany, so each edge is its own row with its own id/timestamps.
 * {@code prerequisites}/{@code dependents} are read-only navigation views
 * (no cascade): TopicDependency rows are created and persisted explicitly
 * through {@link com.learningos.backend.repository.TopicDependencyRepository},
 * since a single edge is referenced from two different parent Topics and
 * double cascading from both sides would fight over the row's lifecycle.
 */
@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
public class Topic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.BEGINNER;

    @Column(nullable = false)
    private int estimatedMinutes;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

    @OneToOne(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private Progress progress;

    @OneToMany(mappedBy = "topic", fetch = FetchType.LAZY)
    private List<TopicDependency> prerequisites = new ArrayList<>();

    @OneToMany(mappedBy = "dependsOnTopic", fetch = FetchType.LAZY)
    private List<TopicDependency> dependents = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
        task.setTopic(this);
    }

    public void addResource(Resource resource) {
        resources.add(resource);
        resource.setTopic(this);
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
        if (progress != null) {
            progress.setTopic(this);
        }
    }
}
