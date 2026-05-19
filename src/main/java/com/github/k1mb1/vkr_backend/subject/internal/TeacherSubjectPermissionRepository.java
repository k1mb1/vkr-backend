package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherSubjectPermissionRepository
    extends JpaRepository<TeacherSubjectPermission, UUID> {

    @Query(
        """
            SELECT DISTINCT p FROM TeacherSubjectPermission p
            JOIN FETCH p.teacher
            JOIN FETCH p.subject subj
            LEFT JOIN FETCH subj.groups
            LEFT JOIN FETCH p.scopes s
            LEFT JOIN FETCH s.group
            LEFT JOIN FETCH s.allowedSubgroup
            WHERE p.subject.id = :subjectId
            """
    )
    List<TeacherSubjectPermission> findBySubjectIdFetchDetails(
        @Param("subjectId") UUID subjectId
    );

    @Query(
        """
            SELECT DISTINCT p FROM TeacherSubjectPermission p
            JOIN FETCH p.teacher
            JOIN FETCH p.subject subj
            LEFT JOIN FETCH subj.groups
            LEFT JOIN FETCH p.scopes s
            LEFT JOIN FETCH s.group
            LEFT JOIN FETCH s.allowedSubgroup
            WHERE p.subject.id = :subjectId
              AND p.teacher.id = :teacherId
            """
    )
    Optional<TeacherSubjectPermission> findBySubjectIdAndTeacherIdFetchDetails(
        @Param("subjectId") UUID subjectId,
        @Param("teacherId") UUID teacherId
    );

    @Query(
        """
            SELECT DISTINCT p FROM TeacherSubjectPermission p
            JOIN FETCH p.teacher
            JOIN FETCH p.subject subj
            LEFT JOIN FETCH subj.groups
            LEFT JOIN FETCH p.scopes s
            LEFT JOIN FETCH s.group
            LEFT JOIN FETCH s.allowedSubgroup
            WHERE p.id = :id
            """
    )
    Optional<TeacherSubjectPermission> findByIdWithDetails(@Param("id") UUID id);

    boolean existsByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);
}
