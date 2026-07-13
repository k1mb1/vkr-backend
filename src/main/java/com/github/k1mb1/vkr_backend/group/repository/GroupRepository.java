package com.github.k1mb1.vkr_backend.group.repository;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, UUID>, JpaSpecificationExecutor<GroupEntity> {
    @EntityGraph(attributePaths = {"subgroups", "students"})
    Optional<GroupEntity> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"subgroups"})
    @Query("SELECT g FROM SubjectEntity s JOIN s.groups g WHERE s.id = :subjectId")
    List<GroupEntity> findBySubjectId(@Param("subjectId") UUID subjectId);
}
