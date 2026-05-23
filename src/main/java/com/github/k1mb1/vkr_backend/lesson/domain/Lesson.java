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

import java.util.HashSet;
import java.util.Set;

@Hidden
@Entity
@Table(
    name = "lessons", indexes = {
    @Index(name = "idx_lessons_subject_id", columnList = "subject_id"),
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

    @Column(name = "order_index", nullable = false) int orderIndex;

    @Column(columnDefinition = "text") String topic;

    @OneToMany(
        mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Builder.Default
    Set<LessonScope> scopes = new HashSet<>();
}
