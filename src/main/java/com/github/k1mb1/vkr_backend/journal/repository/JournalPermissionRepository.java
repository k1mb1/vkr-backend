package com.github.k1mb1.vkr_backend.journal.repository;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/** Read-only доступ модуля journal к выданным правам (subject::domain) — видимость таблиц. */
public interface JournalPermissionRepository extends Repository<TeacherSubjectPermissionEntity, UUID> {

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermissionEntity> findWithDetailsById(UUID id);
}
