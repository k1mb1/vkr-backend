package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.based.BaseEntity;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonTaskEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Grade for a single student on a single {@link LessonTaskEntity}.
 *
 * <p>UNIQUE constraint {@code (task_id, student_id)} ensures one grade per
 * student per task. The endpoint uses upsert semantics.
 *
 * <p>{@code submittedAt} — the moment the student submitted / the teacher
 * recorded the grade. {@code null} = task issued but not yet submitted.
 */
@Entity
@Table(
    name = "student_task_grades",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_TASK_GRADE_TASK_STUDENT",
        columnNames = {"task_id", "student_id"}
    )
)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentTaskGradeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    LessonTaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    StudentEntity student;

    Integer value;

    String comment;

    /** When the student submitted their work. Null = not yet submitted. */
    @Column(name = "submitted_at")
    Instant submittedAt;
}
