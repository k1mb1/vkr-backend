package com.github.k1mb1.vkr_backend.domain.student_groups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentGroupRepository
    extends
        JpaRepository<StudentGroupEntity, UUID>,
        JpaSpecificationExecutor<StudentGroupEntity>
{
    @EntityGraph(attributePaths = "students")
    Optional<StudentGroupEntity> findWithStudentsById(UUID id);
}
