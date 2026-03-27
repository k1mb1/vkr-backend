package com.github.k1mb1.vkr_backend.domain.students;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentRepository
    extends
        JpaRepository<StudentEntity, UUID>,
        JpaSpecificationExecutor<StudentEntity>
{
    List<StudentEntity> findAllByUsernameIn(List<String> usernames);

    List<StudentEntity> findAllBySubjects_Id(UUID subjectId);

    Page<StudentEntity> findAllBySubjects_Id(UUID subjectId, Pageable pageable);
}
