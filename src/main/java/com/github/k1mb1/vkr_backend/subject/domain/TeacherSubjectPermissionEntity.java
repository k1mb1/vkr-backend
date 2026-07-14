package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Hidden
@Entity
@Table(name = "teacher_subject_permissions")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectPermissionEntity extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    TeacherEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    SubjectEntity subject;

    // Authorization-level flag: when true the teacher has full access to the subject and
    // `scopes` must stay empty. Distinct from LessonScopeEntity.allGroups which is a data-shape flag
    // describing a single lesson held jointly for all groups of the subject.
    @Column(name = "all_permissions", nullable = false)
    boolean allPermissions;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    Set<PermissionScopeEntity> scopes = new HashSet<>();
}
