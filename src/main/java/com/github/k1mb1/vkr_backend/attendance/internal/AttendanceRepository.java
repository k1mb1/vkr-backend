package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByLessonIdInAndStudentIdIn(
        Collection<UUID> lessonIds,
        Collection<UUID> studentIds
    );

    Optional<Attendance> findByStudentIdAndLessonId(
        UUID studentId,
        UUID lessonId
    );
}
