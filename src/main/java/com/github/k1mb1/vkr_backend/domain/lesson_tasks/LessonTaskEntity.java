package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import com.github.k1mb1.vkr_backend.domain.based.BaseEntity;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * A task (assignment) attached to a lesson.
 *
 * <p>Displacement penalty configuration (penaltyMode, penaltyStep, issuedTaskIndex)
 * is stored at the lesson level and shared by all tasks in the lesson.
 * The front-end computes the displacement coefficient for each task:
 * <pre>
 *   d = lesson.issuedTaskIndex - task.position
 *   NONE:     coeff = 1.0
 *   SUBTRACT: coeff = max(0, 1 - lesson.penaltyStep * d)
 *   MULTIPLY: coeff = lesson.penaltyStep ^ d
 * </pre>
 */
@Entity
@Table(name = "lesson_tasks")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class LessonTaskEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonEntity lesson;

    @Column(nullable = false)
    @ToString.Include
    String title;

    @Column(columnDefinition = "text")
    String description;

    @Column(nullable = false)
    int maxPoints;

    /**
     * 0-based display/ordering position within the lesson.
     * Used by the front-end together with {@code lesson.issuedTaskIndex}
     * to compute displacement.
     */
    @Column(nullable = false)
    @Builder.Default
    int position = 0;

    /**
     * Whether this task is mandatory.
     *
     * <p>Mandatory tasks ({@code true}, default) are always counted in the
     * student's total, even if the student did not submit — they receive 0.
     * Non-mandatory (bonus) tasks contribute only when submitted.
     */
    @Column(nullable = false)
    @Builder.Default
    boolean isMandatory = true;

    /**
     * Optional submission deadline for this task.
     * {@code null} means there is no hard deadline.
     * The front-end uses this to highlight overdue submissions and may apply
     * an additional late-submission penalty if configured.
     */
    @Column(name = "deadline")
    Instant deadline;
}
