package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInSessionRepository extends JpaRepository<CheckInSession, UUID> {

    /** id предмета сессии (через scope → занятие) — для проверки доступа по id сессии. */
    @Query("SELECT s.lessonScope.lesson.subject.id FROM CheckInSession s WHERE s.id = :id")
    Optional<UUID> findSubjectIdById(UUID id);

    @EntityGraph(
            attributePaths = {
                "lessonScope",
                "lessonScope.lesson",
                "lessonScope.lesson.subject",
                "lessonScope.group",
                "lessonScope.allowedSubgroup"
            })
    Optional<CheckInSession> findWithDetailsById(UUID id);

    Optional<CheckInSession> findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(UUID lessonScopeId);

    @EntityGraph(attributePaths = {"lessonScope", "lessonScope.lesson", "lessonScope.lesson.subject"})
    List<CheckInSession> findByLessonScopeIdInOrderByStartedAtDesc(Collection<UUID> lessonScopeIds);
}
