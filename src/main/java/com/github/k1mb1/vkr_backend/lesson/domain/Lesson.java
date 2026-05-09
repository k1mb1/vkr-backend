package com.github.k1mb1.vkr_backend.lesson.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_offering", columnList = "offering_id"),
    @Index(name = "idx_lesson_started_at", columnList = "started_at"),
    @Index(name = "idx_lesson_teacher", columnList = "teacher_id")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    SubjectOffering offering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id")
    Subgroup subgroup;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "lesson_type", nullable = false, columnDefinition = "lesson_type")
    LessonType type = LessonType.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    Teacher teacher;

    @Column(name = "started_at", nullable = false, columnDefinition = "timestamptz")
    Instant startedAt;

    @Column(name = "ended_at", columnDefinition = "timestamptz")
    Instant endedAt;

    @Column(columnDefinition = "text")
    String topic;
}
