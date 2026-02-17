package com.github.k1mb1.vkr_backend.domain.student_grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StudentGradeRepository extends JpaRepository<StudentGradeEntity, UUID>, JpaSpecificationExecutor<StudentGradeEntity> {
}