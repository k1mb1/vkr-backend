package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subjects")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@NamedEntityGraph(
        name = "Subject.withAssociations",
        attributeNodes = {
                @NamedAttributeNode("teachers"),
                @NamedAttributeNode("students"),
                @NamedAttributeNode("lessons")
        }
)
public class SubjectEntity extends AuditableBase {

    @Column(nullable = false)
    @ToString.Include
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_teacher",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @Builder.Default
    Set<TeacherEntity> teachers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_student",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Builder.Default
    Set<StudentEntity> students = new HashSet<>();

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<LessonEntity> lessons = new HashSet<>();
}