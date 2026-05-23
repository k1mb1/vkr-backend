package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface AttendanceRepository
    extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByLessonScopeIdInAndStudentIdIn(
        Collection<UUID> lessonScopeIds,
        Collection<UUID> studentIds
    );

    Optional<Attendance> findByStudentIdAndLessonScopeId(UUID studentId, UUID lessonScopeId);
}
