package com.github.k1mb1.vkr_backend.domain.student_grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface StudentGradeRepository extends JpaRepository<StudentGradeEntity, UUID>, JpaSpecificationExecutor<StudentGradeEntity> {
    List<StudentGradeEntity> findAllByLesson_Id(UUID lessonId);

    List<StudentGradeEntity> findAllByStudent_Id(UUID studentId);

    List<StudentGradeEntity> findAllByLesson_Subject_Id(UUID subjectId);

    List<StudentGradeEntity> findAllByStudent_IdAndLesson_Subject_Id(UUID studentId, UUID subjectId);
}