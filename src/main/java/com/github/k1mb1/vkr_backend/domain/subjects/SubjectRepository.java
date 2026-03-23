package com.github.k1mb1.vkr_backend.domain.subjects;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository
    extends
        JpaRepository<SubjectEntity, UUID>,
        JpaSpecificationExecutor<SubjectEntity>
{
    @EntityGraph(
        value = "Subject.withAssociations",
        type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<SubjectEntity> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = "students")
    Optional<SubjectEntity> findWithStudentsById(UUID id);

    @EntityGraph(attributePaths = "teachers")
    Optional<SubjectEntity> findWithTeachersById(UUID id);

    List<SubjectEntity> findAllByTeachers_Id(UUID teacherId);

    List<SubjectEntity> findAllByStudents_Id(UUID studentId);
}
