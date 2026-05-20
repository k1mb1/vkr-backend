package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface AssignmentRepository
    extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByLessonIdInOrderByLessonIdAscOrderAsc(Collection<UUID> lessonIds);

    Optional<Assignment> findByLessonIdAndOrder(UUID lessonId, int order);

    boolean existsByLessonId(UUID lessonId);
}
