package com.github.k1mb1.vkr_backend.domain.student_grades;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGradeRepository extends JpaRepository<StudentGradeEntity, UUID> {

    @Query("""
        SELECT g FROM StudentGradeEntity g
        WHERE g.lesson.subject.id = :subjectId
          AND g.student.id IN :studentIds
    """)
    List<StudentGradeEntity> findAllBySubjectIdAndStudentIdIn(
        @Param("subjectId") UUID subjectId,
        @Param("studentIds") List<UUID> studentIds
    );
}
