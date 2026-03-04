package com.github.k1mb1.vkr_backend.domain.students;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
    List<StudentEntity> findAllBySubjects_Id(UUID subjectId);

    Page<StudentEntity> findAllBySubjects_Id(UUID subjectId, Pageable pageable);

    List<StudentEntity> findAllByGroup_Id(UUID groupId);
}