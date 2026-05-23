package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository
    extends JpaRepository<Lesson, UUID>, JpaSpecificationExecutor<Lesson> {

    @EntityGraph(attributePaths = { "subject", "scopes", "scopes.group", "scopes.allowedSubgroup" })
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
}
