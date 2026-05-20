package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
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
    @Query(
        """
            SELECT l FROM Lesson l
            JOIN FETCH l.subject
            LEFT JOIN FETCH l.scopes sc
            LEFT JOIN FETCH sc.group
            LEFT JOIN FETCH sc.allowedSubgroup
            WHERE l.id = :id
            """
    )
    Optional<Lesson> findByIdWithDetails(
        @Param("id") UUID id
    );
}
