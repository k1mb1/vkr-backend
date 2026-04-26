package com.github.k1mb1.vkr_backend.domain.student_attendances;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentAttendanceRepository
    extends JpaRepository<StudentAttendanceEntity, UUID> {

    Optional<StudentAttendanceEntity> findByLesson_IdAndStudent_Id(
        UUID lessonId,
        UUID studentId
    );

    @Query("""
        SELECT a FROM StudentAttendanceEntity a
        JOIN FETCH a.student s
        JOIN FETCH a.lesson l
        WHERE l.subject.id = :subjectId
    """)
    List<StudentAttendanceEntity> findAllBySubjectId(@Param("subjectId") UUID subjectId);
}
