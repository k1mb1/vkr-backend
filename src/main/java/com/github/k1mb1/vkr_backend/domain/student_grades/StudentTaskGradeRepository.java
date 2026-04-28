package com.github.k1mb1.vkr_backend.domain.student_grades;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentTaskGradeRepository
    extends
        JpaRepository<StudentTaskGradeEntity, UUID>,
        JpaSpecificationExecutor<StudentTaskGradeEntity>
{

    Optional<StudentTaskGradeEntity> findByTask_IdAndStudent_Id(
        UUID taskId,
        UUID studentId
    );

    List<StudentTaskGradeEntity> findAllByTask_IdIn(List<UUID> taskIds);

    @Query("""
        SELECT g FROM StudentTaskGradeEntity g
        WHERE g.task.lesson.id = :lessonId
    """)
    List<StudentTaskGradeEntity> findAllByLessonId(@Param("lessonId") UUID lessonId);

    /**
     * Loads all task grades for a subject in one query — used by the grade-sheet
     * endpoint. Joins through task → lesson → subject to filter by subject.
     */
    @Query("""
        SELECT g FROM StudentTaskGradeEntity g
        JOIN FETCH g.task t
        JOIN FETCH t.lesson l
        JOIN FETCH g.student s
        WHERE l.subject.id = :subjectId
    """)
    List<StudentTaskGradeEntity> findAllBySubjectId(@Param("subjectId") UUID subjectId);
}
