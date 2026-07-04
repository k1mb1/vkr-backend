package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherSubjectPermissionRepository extends JpaRepository<TeacherSubjectPermission, UUID> {

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    List<TeacherSubjectPermission> findAllBySubjectId(UUID subjectId);

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermission> findBySubjectIdAndTeacherId(UUID subjectId, UUID teacherId);

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermission> findWithDetailsById(UUID id);

    boolean existsByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);

    /**
     * Лёгкая проекция для снапшота прав: id выданных permission'ов, предметов и признак
     * полного доступа одного преподавателя одним запросом, без подтягивания scope'ов.
     */
    @Query("""
        SELECT p.id AS permissionId, p.subject.id AS subjectId, p.allPermissions AS allPermissions
        FROM TeacherSubjectPermission p
        WHERE p.teacher.id = :teacherId
        """)
    List<OwnedPermissionView> findOwnedByTeacherId(UUID teacherId);

    /** Предмет, к которому относится выданное право — для авторизации управления по id права. */
    @Query("SELECT p.subject.id FROM TeacherSubjectPermission p WHERE p.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);

    interface OwnedPermissionView {
        UUID getPermissionId();

        UUID getSubjectId();

        boolean getAllPermissions();
    }
}
