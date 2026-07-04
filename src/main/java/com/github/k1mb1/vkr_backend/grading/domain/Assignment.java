package com.github.k1mb1.vkr_backend.grading.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Hidden
@Entity
@Table(
        name = "assignments",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_assignment_lesson_order",
                        columnNames = {"lesson_id", "order"}),
        indexes = {
            @Index(name = "idx_assignments_lesson_id", columnList = "lesson_id"),
        })
@NamedEntityGraph(name = "Assignment.withLesson", attributeNodes = @NamedAttributeNode("lesson"))
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    Lesson lesson;

    @Column(name = "\"order\"", nullable = false)
    int order;

    @Column(name = "max_points", nullable = false)
    int maxPoints;

    @Column(nullable = false)
    boolean required;

    /** Режим допуска: используется ли задание как условие для итоговой оценки. */
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_mode", length = 16)
    @Builder.Default
    AssignmentAdmissionMode admissionMode = AssignmentAdmissionMode.NONE;

    /** MIN_SCORE: минимальный балл для прохождения допуска. */
    @Column(name = "admission_min_score")
    Integer admissionMinScore;

    /** TIERED: уровни допуска по убыванию старшинства. Пусто при admissionMode != TIERED. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "assignment_admission_tiers", joinColumns = @JoinColumn(name = "assignment_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    List<AssignmentAdmissionTier> admissionTiers = new ArrayList<>();
}
