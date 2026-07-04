package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
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
public interface GroupRepository extends JpaRepository<Group, UUID>, JpaSpecificationExecutor<Group> {
    @EntityGraph(attributePaths = {"subgroups", "students"})
    Optional<Group> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"subgroups"})
    @Query("SELECT g FROM Subject s JOIN s.groups g WHERE s.id = :subjectId")
    List<Group> findBySubjectId(@Param("subjectId") UUID subjectId);
}
