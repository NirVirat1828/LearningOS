package com.learningos.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A Module belongs to exactly one Course and owns a list of Topics.
 * Named {@code Module} within our own package, so it does not collide with
 * {@code java.lang.Module} despite the implicit java.lang import.
 */
@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
public class Module extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics = new ArrayList<>();

    public void addTopic(Topic topic) {
        topics.add(topic);
        topic.setModule(this);
    }
}
