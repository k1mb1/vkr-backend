package com.github.k1mb1.vkr_backend.domain.student_attendances;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentAttendanceRepository
    extends JpaRepository<StudentAttendanceEntity, UUID> {

    List<StudentAttendanceEntity> findAllByLesson_Id(UUID lessonId);

    /**
     * Loads all attendance rows for all lessons of a subject in one query.
     * Used by the grade-sheet endpoint.
     */
    @Query("""
        SELECT a FROM StudentAttendanceEntity a
        JOIN FETCH a.student s
        WHERE a.lesson.subject.id = :subjectId
    """)
    List<StudentAttendanceEntity> findAllBySubjectId(@Param("subjectId") UUID subjectId);
}
