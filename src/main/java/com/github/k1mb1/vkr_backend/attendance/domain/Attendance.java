package com.github.k1mb1.vkr_backend.attendance.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "attendances",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_student_lesson",
        columnNames = {"student_id", "lesson_id"}
    ),
    indexes = {
        @Index(name = "idx_attendance_lesson", columnList = "lesson_id"),
        @Index(name = "idx_attendance_student", columnList = "student_id")
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
    @JoinColumn(name = "lesson_id", nullable = false)
    Lesson lesson;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(nullable = false, columnDefinition = "attendance_status")
    AttendanceStatus status = AttendanceStatus.NONE;

    @Column(columnDefinition = "text")
    String comment;
}
