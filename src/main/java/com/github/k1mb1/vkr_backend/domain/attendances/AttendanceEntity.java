package com.github.k1mb1.vkr_backend.domain.attendances;

import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "student_attendances")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class AttendanceEntity extends AuditableBase {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonEntity lesson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    StudentEntity student;

    @Enumerated(EnumType.STRING)
    @Column(name = "presence", nullable = false, columnDefinition = "PresenceType")
    @Builder.Default
    PresenceType presence = PresenceType.NONE;

    String note;
}