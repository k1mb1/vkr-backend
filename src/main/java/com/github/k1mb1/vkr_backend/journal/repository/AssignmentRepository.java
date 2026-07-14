package com.github.k1mb1.vkr_backend.journal.repository;

import com.github.k1mb1.vkr_backend.journal.domain.AssignmentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
    @EntityGraph("Assignment.withLesson")
    List<AssignmentEntity> findByLessonIdInOrderByLessonIdAscOrderAsc(Collection<UUID> lessonIds);

    boolean existsByLessonId(UUID lessonId);

    long deleteByLessonId(UUID lessonId);

    /** id предмета, которому принадлежит задание — для проверки доступа по id задания. */
    @Query("SELECT a.lesson.subject.id FROM AssignmentEntity a WHERE a.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);
}
