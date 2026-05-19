package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Hidden
@Entity
@Table(name = "teacher_subject_permission_scopes")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PermissionScope extends ArchivableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    TeacherSubjectPermission permission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allowed_subgroup_id")
    Subgroup allowedSubgroup;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "allowed_lesson_type", columnDefinition = "lesson_type")
    LessonType allowedLessonType;
}
