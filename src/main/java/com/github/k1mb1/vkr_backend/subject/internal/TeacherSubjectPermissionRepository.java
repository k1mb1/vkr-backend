package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherSubjectPermissionRepository
    extends JpaRepository<TeacherSubjectPermission, UUID> {

    @EntityGraph(
        attributePaths = {
            "teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"
        }
    )
    List<TeacherSubjectPermission> findAllBySubjectId(UUID subjectId);

    @EntityGraph(
        attributePaths = {
            "teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"
        }
    )
    Optional<TeacherSubjectPermission> findBySubjectIdAndTeacherId(UUID subjectId, UUID teacherId);

    @EntityGraph(
        attributePaths = {
            "teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"
        }
    )
    Optional<TeacherSubjectPermission> findWithDetailsById(UUID id);

    boolean existsByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);
}
