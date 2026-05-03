package com.github.k1mb1.vkr_backend.education.attendance.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAttendanceRepository extends JpaRepository<StudentAttendanceEntity, UUID> {
    Optional<StudentAttendanceEntity> findByLessonIdAndStudentId(UUID lessonId, UUID studentId);
    List<StudentAttendanceEntity> findByLessonIdIn(Collection<UUID> lessonIds);
}
