package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import com.github.k1mb1.vkr_backend.domain.based.BaseEntity;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A task (assignment) attached to a lesson.
 *
 * <p>Displacement (vытеснение) semantics stored here:
 * <ul>
 *   <li>{@code issuedTaskIndex} — the {@link #position} of the <em>latest</em>
 *       task that has been issued in this lesson at the time the task was last
 *       updated.  The front-end uses this together with {@code penaltyMode} and
 *       {@code penaltyStep} to compute a per-task displacement coefficient.</li>
 *   <li>{@code penaltyMode} — {@link PenaltyMode#SUBTRACT} or
 *       {@link PenaltyMode#MULTIPLY}.</li>
 *   <li>{@code penaltyStep} — step value for the chosen mode (e.g. 0.25 for
 *       SUBTRACT, 0.5 for MULTIPLY).</li>
 * </ul>
 *
 * <p>Front-end formula (given task with {@code position = k},
 * {@code issuedTaskIndex = n}):
 * <pre>
 *   d = n - k          // how many steps back this task is
 *   SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
 *   MULTIPLY: coeff = penaltyStep ^ d
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
     * The front-end uses {@code position} together with {@code issuedTaskIndex}
     * to calculate displacement.
     */
    @Column(nullable = false)
    @Builder.Default
    int position = 0;

    /**
     * The position of the most recently issued task in this lesson.
     * Set/updated by the teacher when a new task is issued.
     * Stored on the task so the coefficient is reproducible without
     * extra context.
     */
    @Column(nullable = false)
    @Builder.Default
    int issuedTaskIndex = 0;

    /** How to decay the coefficient for superseded tasks. */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    PenaltyMode penaltyMode = PenaltyMode.SUBTRACT;

    /**
     * Step value for the penalty calculation.
     * Typical values: 0.25 (SUBTRACT) or 0.5 (MULTIPLY).
     */
    @Column(nullable = false, precision = 5, scale = 4)
    @Builder.Default
    BigDecimal penaltyStep = new BigDecimal("0.25");

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
