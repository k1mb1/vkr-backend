package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface CheckInSessionRepository
    extends JpaRepository<CheckInSession, UUID> {

    @EntityGraph(
        attributePaths = {
            "lessonScope",
            "lessonScope.lesson",
            "lessonScope.lesson.subject",
            "lessonScope.group",
            "lessonScope.allowedSubgroup"
        }
    )
    Optional<CheckInSession> findWithDetailsById(UUID id);

    Optional<CheckInSession> findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(
        UUID lessonScopeId
    );

    @EntityGraph(
        attributePaths = {
            "lessonScope", "lessonScope.lesson", "lessonScope.lesson.subject"
        }
    )
    List<CheckInSession> findByLessonScopeIdInOrderByStartedAtDesc(
        Collection<UUID> lessonScopeIds
    );
}
