package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
interface AssignmentRepository
    extends JpaRepository<Assignment, UUID> {

    @Query("""
        SELECT a FROM Assignment a
        JOIN FETCH a.lesson l
        WHERE l.id IN :lessonIds
        ORDER BY l.startedAt, a.title
        """)
    List<Assignment> findByLessonIdsOrdered(@Param("lessonIds") Collection<UUID> lessonIds);
}
