package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface CheckInSessionRepository
    extends JpaRepository<CheckInSession, UUID> {

    @Query(
        """
        SELECT s FROM CheckInSession s
        JOIN FETCH s.lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.group
        LEFT JOIN FETCH l.subgroup
        JOIN FETCH s.permission p
        LEFT JOIN FETCH p.allowedSubgroup
        WHERE s.id = :id
        """
    )
    Optional<CheckInSession> findByIdWithDetails(@Param("id") UUID id);

    Optional<CheckInSession> findByLessonIdAndConfirmedAtIsNullAndCancelledAtIsNull(UUID lessonId);

    List<CheckInSession> findByPermissionIdOrderByStartedAtDesc(UUID permissionId);
}
