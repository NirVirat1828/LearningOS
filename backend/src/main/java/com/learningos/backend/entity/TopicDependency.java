package com.learningos.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * A directed prerequisite edge in the Topic dependency graph: {@code topic}
 * requires {@code dependsOnTopic} to be completed first. Modeling this as
 * its own entity (instead of a plain @ManyToMany) is what lets each edge
 * carry its own id and audit timestamps, and lets both self-referencing
 * sides of Topic map to it independently.
 * <p>
 * Both foreign keys use {@code ON DELETE CASCADE} at the database level
 * ({@link OnDelete}) rather than JPA-level cascade: a single row here is
 * reachable from two different Topics (as {@code topic} and as
 * {@code dependsOnTopic}), so letting the database clean up orphaned edges
 * on delete avoids the two parent collections fighting over the row's
 * lifecycle the way dual JPA cascades would.
 */
@Entity
@Table(
        name = "topic_dependencies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"topic_id", "depends_on_topic_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class TopicDependency extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_topic_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Topic dependsOnTopic;

    public TopicDependency(Topic topic, Topic dependsOnTopic) {
        this.topic = topic;
        this.dependsOnTopic = dependsOnTopic;
    }
}
