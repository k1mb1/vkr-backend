package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.education.assignments.api.SubmissionStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "student_task_grades",
    uniqueConstraints = @UniqueConstraint(name = "UK_TASK_GRADE_TASK_STUDENT", columnNames = {"task_id","student_id"}))
@NoArgsConstructor
@Getter @Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentTaskGradeEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    LessonTaskEntity task;

    @Column(name = "student_id", nullable = false)
    UUID studentId;

    Integer value;
    String comment;

    @Column(name = "submitted_at")
    Instant submittedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    SubmissionStatus status = SubmissionStatus.NOT_SUBMITTED;
}
