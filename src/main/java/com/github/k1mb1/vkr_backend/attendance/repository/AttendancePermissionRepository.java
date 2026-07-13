package com.github.k1mb1.vkr_backend.attendance.repository;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/** Read-only доступ модуля attendance к выданным правам (subject::domain) для таблиц посещаемости. */
public interface AttendancePermissionRepository extends Repository<TeacherSubjectPermissionEntity, UUID> {

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermissionEntity> findWithDetailsById(UUID id);
}
