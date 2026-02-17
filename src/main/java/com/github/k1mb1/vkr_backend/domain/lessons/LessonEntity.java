package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.student_attendances.StudentAttendanceEntity;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentGradeEntity;
import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class LessonEntity extends AuditableBase {

    @Column(nullable = false)
    @ToString.Include
    String name;

    Instant dateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    SubjectEntity subject;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    LessonType type = LessonType.NONE;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<StudentAttendanceEntity> attendances = new HashSet<>();

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<StudentGradeEntity> grades = new HashSet<>();
}