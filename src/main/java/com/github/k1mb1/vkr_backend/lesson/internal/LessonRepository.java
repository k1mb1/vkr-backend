package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository
    extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson>
{
    @Query(
        """
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.group
        LEFT JOIN FETCH l.subgroup
        WHERE EXISTS (
            SELECT 1 FROM TeacherSubjectPermission p
            WHERE p.id = :permissionId
              AND p.subject = l.subject
              AND p.group = l.group
              AND (p.allowedSubgroup IS NULL OR p.allowedSubgroup = l.subgroup)
              AND (p.allowedLessonType IS NULL OR p.allowedLessonType = l.type)
        )
        ORDER BY l.startedAt
        """
    )
    List<Lesson> findAllByPermissionIdWithDetails(
        @Param("permissionId") UUID permissionId
    );

    @Query(
        """
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.group
        LEFT JOIN FETCH l.subgroup
        WHERE l.id = :id
        """
    )
    Optional<Lesson> findByIdWithDetails(@Param("id") UUID id);
}
