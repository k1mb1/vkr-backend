package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.based.BaseEntity;
import com.github.k1mb1.vkr_backend.domain.student_attendances.StudentAttendanceEntity;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentGradeEntity;
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

    @Column(nullable = false)
    @Builder.Default
    boolean archived = false;

    @Column(name = "archived_at")
    Instant archivedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    SubjectEntity subject;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    LessonType type = LessonType.NONE;

    /**
     * Target group or subgroup for this lesson.
     * NULL = lesson is for all students enrolled in the subject (e.g. a lecture for everyone).
     * Non-null = lesson is restricted to students belonging to this group/subgroup.
     */
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

    @OneToMany(
        mappedBy = "lesson",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    Set<StudentGradeEntity> grades = new HashSet<>();

    /**
     * Decay coefficient for the whole lesson [0..1].
     * 1.0 = no decay.  The front-end multiplies the summed weighted task scores
     * by this value when computing a student's contribution from this lesson.
     */
    @Column(nullable = false, precision = 5, scale = 4)
    @Builder.Default
    BigDecimal decayFactor = BigDecimal.ONE;

    /**
     * Tasks (assignments) belonging to this lesson, ordered by position.
     */
    @OneToMany(
        mappedBy = "lesson",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    List<LessonTaskEntity> tasks = new ArrayList<>();
}
