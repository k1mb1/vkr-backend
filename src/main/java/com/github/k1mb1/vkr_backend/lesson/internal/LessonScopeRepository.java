package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonScopeRepository
    extends JpaRepository<LessonScope, UUID> {

    @Query(
        """
            SELECT ls FROM LessonScope ls
            JOIN FETCH ls.lesson l
            JOIN FETCH l.subject
            JOIN FETCH ls.group
            LEFT JOIN FETCH ls.allowedSubgroup
            WHERE ls.id = :id
            """
    )
    Optional<LessonScope> findByIdWithDetails(
        @Param("id") UUID id
    );
}
