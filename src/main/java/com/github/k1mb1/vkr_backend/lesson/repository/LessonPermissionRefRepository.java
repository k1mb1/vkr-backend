package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/** Read-only доступ модуля lesson к выданным правам (subject::domain) для фильтрации занятий. */
public interface LessonPermissionRefRepository extends Repository<TeacherSubjectPermissionEntity, UUID> {

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermissionEntity> findWithDetailsById(UUID id);
}
