package com.github.k1mb1.vkr_backend.attendance.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Hidden
@Entity
@Table(
        name = "attendances",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_attendance_student_lesson_scope",
                        columnNames = {"student_id", "lesson_scope_id"}),
        indexes = {
            @Index(name = "idx_attendances_lesson_scope_id", columnList = "lesson_scope_id"),
            @Index(name = "idx_attendances_student_id", columnList = "student_id"),
        })
@NamedEntityGraph(
        name = "Attendance.withDetails",
        attributeNodes = {
            @NamedAttributeNode("student"),
            @NamedAttributeNode("lessonScope"),
        })
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_scope_id", nullable = false)
    LessonScopeEntity lessonScope;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "attendance_status")
    AttendanceStatus status;

    @Column(columnDefinition = "text")
    @Nullable String comment;
}
