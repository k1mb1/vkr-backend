package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
interface GradeRepository
    extends JpaRepository<Grade, UUID> {

    List<Grade> findByLessonIdInAndStudentIdIn(
        Collection<UUID> lessonIds,
        Collection<UUID> studentIds
    );
}
