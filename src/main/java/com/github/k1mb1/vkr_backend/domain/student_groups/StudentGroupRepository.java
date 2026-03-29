package com.github.k1mb1.vkr_backend.domain.student_groups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentGroupRepository
    extends
        JpaRepository<StudentGroupEntity, UUID>,
        JpaSpecificationExecutor<StudentGroupEntity> {

    /** Find a top-level group (no parent) by name. */
    Optional<StudentGroupEntity> findByNameAndParentGroupIsNull(String name);

    /** Find a subgroup by name under a specific parent group. */
    Optional<StudentGroupEntity> findByNameAndParentGroup_Id(String name, UUID parentGroupId);
}
