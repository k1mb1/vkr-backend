package com.github.k1mb1.vkr_backend.domain.student_grades;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentTaskGradeRepository
    extends JpaRepository<StudentTaskGradeEntity, UUID> {

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
}
