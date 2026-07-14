package com.github.k1mb1.vkr_backend.journal.repository;

import com.github.k1mb1.vkr_backend.journal.domain.AttendanceEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, UUID> {
    @EntityGraph("Attendance.withDetails")
    List<AttendanceEntity> findByLessonScopeIdInAndStudentIdIn(
            Collection<UUID> lessonScopeIds, Collection<UUID> studentIds);
}
