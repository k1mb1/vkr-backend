package com.github.k1mb1.vkr_backend.domain.teachers;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository extends JpaRepository<TeacherEntity, UUID>, JpaSpecificationExecutor<TeacherEntity> {
    @EntityGraph(value = "Teacher.withSubjects", type = EntityGraph.EntityGraphType.LOAD)
    Optional<TeacherEntity> findWithSubjectsById(UUID id);
}