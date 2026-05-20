package com.github.k1mb1.vkr_backend.grade.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Hidden
@Entity
@Table(
    name = "grades",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_grade_student_assignment",
        columnNames = {"student_id", "assignment_id"}
    ),
    indexes = {
        @Index(name = "idx_grades_assignment_id", columnList = "assignment_id"),
        @Index(name = "idx_grades_student_id", columnList = "student_id"),
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Grade
    extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    Assignment assignment;

    @Column(nullable = false)
    int value;

    @Column(columnDefinition = "text")
    String comment;
}
