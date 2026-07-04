package com.github.k1mb1.vkr_backend.grading.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

@Hidden
@Entity
@Table(
        name = "grades",
        indexes = {
            @Index(name = "idx_grades_lesson_id", columnList = "lesson_id"),
            @Index(name = "idx_grades_student_id", columnList = "student_id"),
            @Index(name = "idx_grades_assignment_id", columnList = "assignment_id"),
        })
@NamedEntityGraph(
        name = "Grade.withDetails",
        attributeNodes = {
            @NamedAttributeNode("student"),
            @NamedAttributeNode("lesson"),
            @NamedAttributeNode("assignment"),
            @NamedAttributeNode("awardedLesson"),
        })
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Grade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @Nullable Assignment assignment;

    // Активное занятие (того же типа, что и занятие задания) на момент выставления —
    // след для UI; фиксируется при создании ячейки. null для оценок вне задания.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "awarded_lesson_id")
    @Nullable Lesson awardedLesson;

    // Знаковое смещение сдачи в занятиях своего типа, считанное только по занятиям с заданиями:
    // разность «рангов» активного и заданного занятий среди занятий этого типа, у которых есть задания.
    // >0 — позже срока (штраф), <0 — раньше (бонус), 0 — вовремя. Зафиксировано при создании ячейки.
    // null — оценка вне задания или активного занятия не было. Фронт по нему применяет политику.
    @Column(name = "lessons_offset")
    @Nullable Integer lessonsOffset;

    @Column(nullable = false)
    int score;

    @Column(columnDefinition = "text")
    @Nullable String comment;
}
