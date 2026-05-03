package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "lesson_tasks")
@NoArgsConstructor
@Getter @Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class LessonTaskEntity extends BaseEntity {

    @Column(name = "lesson_id", nullable = false)
    UUID lessonId;

    @Column(nullable = false)
    @ToString.Include
    String title;

    @Column(columnDefinition = "text")
    String description;

    @Column(nullable = false)
    int maxPoints;

    @Column(nullable = false)
    @Builder.Default
    int position = 0;

    @Column(nullable = false)
    @Builder.Default
    boolean isMandatory = true;

    @Column(name = "deadline")
    Instant deadline;
}
