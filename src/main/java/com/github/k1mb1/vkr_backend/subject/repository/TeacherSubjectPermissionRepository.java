package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherSubjectPermissionRepository extends JpaRepository<TeacherSubjectPermissionEntity, UUID> {

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    List<TeacherSubjectPermissionEntity> findAllBySubjectId(UUID subjectId);

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermissionEntity> findBySubjectIdAndTeacherId(UUID subjectId, UUID teacherId);

    @EntityGraph(attributePaths = {"teacher", "subject", "scopes", "scopes.group", "scopes.allowedSubgroup"})
    Optional<TeacherSubjectPermissionEntity> findWithDetailsById(UUID id);

    boolean existsByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);

    /**
     * Лёгкая проекция для снапшота прав: id выданных permission'ов, предметов и признак
     * полного доступа одного преподавателя одним запросом, без подтягивания scope'ов.
     */
    @Query("""
        SELECT p.id AS permissionId, p.subject.id AS subjectId, p.allPermissions AS allPermissions
        FROM TeacherSubjectPermissionEntity p
        WHERE p.teacher.id = :teacherId
        """)
    List<OwnedPermissionView> findOwnedByTeacherId(UUID teacherId);

    /** Предмет, к которому относится выданное право — для авторизации управления по id права. */
    @Query("SELECT p.subject.id FROM TeacherSubjectPermissionEntity p WHERE p.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);

    interface OwnedPermissionView {
        UUID getPermissionId();

        UUID getSubjectId();

        boolean getAllPermissions();
    }
}
