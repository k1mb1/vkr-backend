package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository
    extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson>
{
    @EntityGraph(
        attributePaths = {
            "subject",
            "scopes",
            "scopes.group",
            "scopes.allowedSubgroup",
        }
    )
    Optional<Lesson> findWithDetailsById(UUID id);

    @Query(
        """
        SELECT MAX(l.orderIndex) FROM Lesson l
        WHERE l.subject.id = :subjectId AND l.type = :type
        """
    )
    Integer findMaxOrderIndex(
        @Param("subjectId") UUID subjectId,
        @Param("type") LessonType type
    );

    @Modifying
    @Query(
        """
        UPDATE Lesson l SET l.orderIndex = l.orderIndex - 1
        WHERE l.subject.id = :subjectId
          AND l.type = :type
          AND l.orderIndex > :afterIndex
        """
    )
    int shiftOrderIndexDown(
        @Param("subjectId") UUID subjectId,
        @Param("type") LessonType type,
        @Param("afterIndex") int afterIndex
    );

    @Modifying
    @Query(
        """
        UPDATE Lesson l SET l.active = false
        WHERE l.subject.id = :subjectId AND l.type = :type AND l.active = true
        """
    )
    int clearActiveForSubjectAndType(
        @Param("subjectId") UUID subjectId,
        @Param("type") LessonType type
    );

    Optional<Lesson> findBySubjectIdAndTypeAndActiveTrue(
        UUID subjectId,
        LessonType type
    );

    @Query(
        """
        SELECT l FROM Lesson l
        WHERE l.subject.id = :subjectId AND l.type = :type
          AND EXISTS (SELECT 1 FROM Assignment a WHERE a.lesson.id = l.id)
        ORDER BY l.orderIndex
        """
    )
    List<Lesson> findWithAssignmentsBySubjectIdAndType(
        @Param("subjectId") UUID subjectId,
        @Param("type") LessonType type
    );
}
