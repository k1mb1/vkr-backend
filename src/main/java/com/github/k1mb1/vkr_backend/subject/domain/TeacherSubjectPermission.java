package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Hidden
@Entity
@Table(name = "teacher_subject_permissions")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectPermission
    extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    Subject subject;

    // Authorization-level flag: when true the teacher has full access to the subject and
    // `scopes` must stay empty. Distinct from LessonScope.allGroups which is a data-shape flag
    // describing a single lesson held jointly for all groups of the subject.
    @Column(name = "all_permissions", nullable = false) boolean allPermissions;

    @OneToMany(
        mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Builder.Default
    Set<PermissionScope> scopes = new HashSet<>();
}
