package com.github.k1mb1.vkr_backend.lesson.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

@Hidden
@Entity
@Table(name = "lesson_scopes")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LessonScopeEntity extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonEntity lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @Nullable GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allowed_subgroup_id")
    @Nullable SubgroupEntity allowedSubgroup;

    @Column(name = "started_at", nullable = false)
    LocalDate startedAt;

    // Data-shape flag for THIS lesson: when true the lesson is held jointly for all groups of
    // the subject (group and allowedSubgroup must be null). Unrelated to
    // TeacherSubjectPermissionEntity.allPermissions, which controls authorization scope.
    @Column(name = "all_groups", nullable = false)
    boolean allGroups;
}
