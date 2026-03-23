package com.github.k1mb1.vkr_backend.domain.teachers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeacherRepository
    extends
        JpaRepository<TeacherEntity, UUID>,
        JpaSpecificationExecutor<TeacherEntity>
{
    @EntityGraph(
        value = "Teacher.withSubjects",
        type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<TeacherEntity> findWithSubjectsById(UUID id);

    List<TeacherEntity> findAllBySubjects_Id(UUID subjectId);

    Page<TeacherEntity> findAllBySubjects_Id(UUID subjectId, Pageable pageable);
}
