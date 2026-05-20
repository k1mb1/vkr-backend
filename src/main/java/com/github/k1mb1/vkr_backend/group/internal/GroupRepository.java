package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository
    extends JpaRepository<Group, UUID>, JpaSpecificationExecutor<Group> {
    @EntityGraph(attributePaths = { "subgroups", "students" })
    Optional<Group> findWithDetailsById(UUID id);
}
