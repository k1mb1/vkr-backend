package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface TeacherSubjectPermissionRepository
    extends JpaRepository<TeacherSubjectPermission, UUID> {
    List<TeacherSubjectPermission> findBySubjectId(UUID subjectId);

    @Query(
        """
            SELECT p FROM TeacherSubjectPermission p
            JOIN FETCH p.teacher
            JOIN FETCH p.group
            LEFT JOIN FETCH p.allowedSubgroup
            WHERE p.subject.id = :subjectId
            ORDER BY p.group.name
            """
    )
    List<TeacherSubjectPermission> findBySubjectIdFetchDetails(
        @Param("subjectId") UUID subjectId
    );

    @Query(
        """
            SELECT p FROM TeacherSubjectPermission p
            JOIN FETCH p.teacher
            JOIN FETCH p.group
            LEFT JOIN FETCH p.allowedSubgroup
            WHERE p.subject.id = :subjectId
              AND p.teacher.id = :teacherId
            ORDER BY p.group.name
            """
    )
    List<TeacherSubjectPermission> findBySubjectIdAndTeacherIdFetchDetails(
        @Param("subjectId") UUID subjectId,
        @Param("teacherId") UUID teacherId
    );

    @Query(
        """
            SELECT p FROM TeacherSubjectPermission p
            WHERE p.teacher.id = :teacherId
              AND p.subject.id = :subjectId
              AND p.group.id = :groupId
              AND p.archivedAt IS NULL
            """
    )
    List<TeacherSubjectPermission> findActiveByTeacherSubjectGroup(
        @Param("teacherId") UUID teacherId,
        @Param("subjectId") UUID subjectId,
        @Param("groupId") UUID groupId
    );
}
