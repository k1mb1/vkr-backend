package com.github.k1mb1.vkr_backend.education.assignments.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentTaskGradeRepository extends JpaRepository<StudentTaskGradeEntity, UUID> {
    Optional<StudentTaskGradeEntity> findByTask_IdAndStudentId(UUID taskId, UUID studentId);
    List<StudentTaskGradeEntity> findAllByTask_IdIn(Collection<UUID> taskIds);
    @Query("SELECT g FROM StudentTaskGradeEntity g WHERE g.task.lessonId = :lessonId")
    List<StudentTaskGradeEntity> findAllByLessonId(@Param("lessonId") UUID lessonId);
}
