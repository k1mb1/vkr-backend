package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// "Учитель ведёт offering — целиком (subgroup=null) или конкретную подгруппу"
// Заменяет @ManyToMany Subject <-> Teacher
//
// ВНИМАНИЕ: уникальность с NULL в Postgres работает через partial index.
// В миграции добавь:
//   CREATE UNIQUE INDEX uk_assignment_full_offering
//     ON subject_assignments (teacher_id, offering_id)
//     WHERE subgroup_id IS NULL;
@Hidden
@Entity
@Table(
    name = "subject_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_assignment_unique",
        columnNames = { "teacher_id", "offering_id", "subgroup_id" }
    ),
    indexes = {
        @Index(name = "idx_assignment_teacher", columnList = "teacher_id"),
        @Index(name = "idx_assignment_offering", columnList = "offering_id"),
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    SubjectOffering offering;

    // null = ведёт всю группу offering'а; не null = только эту подгруппу
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id")
    Subgroup subgroup;

    // null = все типы; LECTURE = только лекции; PRACTICE = только практики
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "lesson_type_scope", columnDefinition = "lesson_type")
    LessonType lessonTypeScope;
}
