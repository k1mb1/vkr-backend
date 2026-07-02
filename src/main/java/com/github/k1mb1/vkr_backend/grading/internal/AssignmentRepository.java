package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    @EntityGraph("Assignment.withLesson")
    List<Assignment> findByLessonIdInOrderByLessonIdAscOrderAsc(
        Collection<UUID> lessonIds
    );

    boolean existsByLessonId(UUID lessonId);

    long deleteByLessonId(UUID lessonId);

    /** id предмета, которому принадлежит задание — для проверки доступа по id задания. */
    @Query("SELECT a.lesson.subject.id FROM Assignment a WHERE a.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);
}
