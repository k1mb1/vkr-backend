package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.based.BaseEntity;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskEntity;
import com.github.k1mb1.vkr_backend.domain.student_attendances.StudentAttendanceEntity;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class LessonEntity extends BaseEntity {

    @Column(nullable = false)
    @ToString.Include
    String name;

    OffsetDateTime dateTime;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    SubjectEntity subject;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    LessonType type = LessonType.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    StudentGroupEntity group;

    @OneToMany(
        mappedBy = "lesson",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    Set<StudentAttendanceEntity> attendances = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    IssuanceMode issuanceMode = IssuanceMode.AUTO;


    @Column(name = "issued_at")
    Instant issuedAt;

    /**
     * Position of the task the teacher is currently presenting to students.
     * Tasks with position < issuedTaskIndex are superseded and receive a
     * displacement penalty (calculated by the front-end using penaltyMode/penaltyStep).
     */
    @Column(nullable = false)
    @Builder.Default
    int issuedTaskIndex = 0;

    /**
     * How the displacement coefficient is computed for superseded tasks.
     * NONE (default) means no penalty — all tasks keep coefficient 1.0.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    PenaltyMode penaltyMode = PenaltyMode.NONE;

    /**
     * Step value for the penalty calculation.
     * Typical values: 0.25 (SUBTRACT) or 0.5 (MULTIPLY).
     * Ignored when penaltyMode is NONE.
     */
    @Column(nullable = false, precision = 5, scale = 4)
    @Builder.Default
    BigDecimal penaltyStep = new BigDecimal("0.25");

    @OneToMany(
        mappedBy = "lesson",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    List<LessonTaskEntity> tasks = new ArrayList<>();
}
