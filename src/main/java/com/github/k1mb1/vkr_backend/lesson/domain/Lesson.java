package com.github.k1mb1.vkr_backend.lesson.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Hidden
@Entity
@Table(
    name = "lessons", indexes = {
    @Index(name = "idx_lessons_subject_id", columnList = "subject_id"),
    @Index(name = "idx_lessons_group_id", columnList = "group_id"),
    @Index(name = "idx_lessons_subgroup_id", columnList = "subgroup_id"),
    @Index(name = "idx_lessons_teacher_id", columnList = "teacher_id"),
    @Index(name = "idx_lessons_started_at", columnList = "started_at"),
}
)
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Lesson
    extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id")
    Subgroup subgroup;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "lesson_type", nullable = false, columnDefinition = "lesson_type"
    )
    LessonType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    Teacher teacher;

    @Column(
        name = "started_at", nullable = false, columnDefinition = "timestamptz"
    ) Instant startedAt;

    @Column(name = "ended_at", columnDefinition = "timestamptz") Instant endedAt;

    @Column(columnDefinition = "text") String topic;
}
