package com.github.k1mb1.vkr_backend.grade.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
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
    name = "assignments",
    indexes = @Index(name = "idx_assignments_lesson_id", columnList = "lesson_id")
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Assignment
    extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    Lesson lesson;

    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    boolean required;

    @Column(nullable = false)
    int maxScore;
}
