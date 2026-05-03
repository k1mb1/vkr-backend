package com.github.k1mb1.vkr_backend.education.subjects.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "subjects")
@NoArgsConstructor
@Getter @Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class SubjectEntity extends BaseEntity {

    @Column(nullable = false)
    @ToString.Include
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    @Builder.Default
    boolean archived = false;

    @Column(name = "archived_at")
    Instant archivedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "subject_teacher", joinColumns = @JoinColumn(name = "subject_id"))
    @Column(name = "teacher_id")
    @Builder.Default
    Set<UUID> teacherIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "subject_student", joinColumns = @JoinColumn(name = "subject_id"))
    @Column(name = "student_id")
    @Builder.Default
    Set<UUID> studentIds = new HashSet<>();
}
