package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface GradeRepository extends JpaRepository<Grade, UUID> {
    @EntityGraph("Grade.withDetails")
    List<Grade> findByLessonIdInAndStudentIdIn(
        Collection<UUID> lessonIds,
        Collection<UUID> studentIds
    );
}
