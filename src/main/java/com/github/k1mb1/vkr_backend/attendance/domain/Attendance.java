package com.github.k1mb1.vkr_backend.attendance.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Hidden
@Entity
@Table(
    name = "attendances",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_student_lesson_scope",
        columnNames = { "student_id", "lesson_scope_id" }
    ),
    indexes = {
        @Index(
            name = "idx_attendances_lesson_scope_id",
            columnList = "lesson_scope_id"
        ),
        @Index(name = "idx_attendances_student_id", columnList = "student_id"),
    }
)
@NamedEntityGraph(
    name = "Attendance.withDetails",
    attributeNodes = {
        @NamedAttributeNode("student"),
        @NamedAttributeNode("lessonScope"),
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_scope_id", nullable = false)
    LessonScope lessonScope;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "attendance_status")
    AttendanceStatus status;

    @Column(columnDefinition = "text")
    String comment;
}
