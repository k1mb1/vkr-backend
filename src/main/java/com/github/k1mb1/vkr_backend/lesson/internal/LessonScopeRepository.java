package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonScopeRepository
    extends JpaRepository<LessonScope, UUID> {

    @EntityGraph(attributePaths = { "lesson", "lesson.subject", "group", "allowedSubgroup" })
    Optional<LessonScope> findWithDetailsById(UUID id);
}
