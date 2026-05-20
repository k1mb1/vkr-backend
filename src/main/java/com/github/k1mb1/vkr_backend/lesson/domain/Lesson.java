package com.github.k1mb1.vkr_backend.lesson.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Hidden
@Entity
@Table(
    name = "lessons", indexes = {
    @Index(name = "idx_lessons_subject_id", columnList = "subject_id"),
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

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "lesson_type", nullable = false, columnDefinition = "lesson_type"
    )
    LessonType type;

    @Column(name = "started_at", nullable = false) LocalDate startedAt;

    @Column(columnDefinition = "text") String topic;

    @Column(name = "all_groups", nullable = false) boolean allGroups;

    @OneToMany(
        mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Builder.Default
    Set<LessonScope> scopes = new HashSet<>();
}
