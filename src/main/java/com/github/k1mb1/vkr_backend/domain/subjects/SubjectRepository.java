package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID>, JpaSpecificationExecutor<SubjectEntity> {
    @EntityGraph(value = "Subject.withAssociations", type = EntityGraph.EntityGraphType.LOAD)
    Optional<SubjectEntity> findWithDetailsById(UUID id);
}