package com.github.k1mb1.vkr_backend.domain.student_groups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupRepository
    extends
        JpaRepository<StudentGroupEntity, UUID>,
        JpaSpecificationExecutor<StudentGroupEntity> {

    /** Find a top-level group (no parent) by name. */
    Optional<StudentGroupEntity> findByNameAndParentGroupIsNull(String name);

    /** Find a subgroup by name under a specific parent group. */
    Optional<StudentGroupEntity> findByNameAndParentGroup_Id(String name, UUID parentGroupId);

    /**
     * Load a main group with its direct students, subgroups and each subgroup's students
     * in a single query to avoid N+1.
     */
    @Query("""
        SELECT DISTINCT g FROM StudentGroupEntity g
        LEFT JOIN FETCH g.students
        LEFT JOIN FETCH g.subgroups sg
        LEFT JOIN FETCH sg.students
        WHERE g.id = :id AND g.parentGroup IS NULL
        """)
    Optional<StudentGroupEntity> findWithSubgroupsAndStudentsById(@Param("id") UUID id);
}
