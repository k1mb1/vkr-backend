package com.github.k1mb1.vkr_backend.journal.checkin.repository;

import com.github.k1mb1.vkr_backend.journal.checkin.domain.CheckInSessionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInSessionRepository extends JpaRepository<CheckInSessionEntity, UUID> {

    /** id предмета сессии (через scope → занятие) — для проверки доступа по id сессии. */
    @Query("SELECT s.lessonScope.lesson.subject.id FROM CheckInSessionEntity s WHERE s.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);

    @EntityGraph(
            attributePaths = {
                "lessonScope",
                "lessonScope.lesson",
                "lessonScope.lesson.subject",
                "lessonScope.group",
                "lessonScope.allowedSubgroup"
            })
    Optional<CheckInSessionEntity> findWithDetailsById(UUID id);

    Optional<CheckInSessionEntity> findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(UUID lessonScopeId);

    @EntityGraph(attributePaths = {"lessonScope", "lessonScope.lesson", "lessonScope.lesson.subject"})
    List<CheckInSessionEntity> findByLessonScopeIdInOrderByStartedAtDesc(Collection<UUID> lessonScopeIds);
}
