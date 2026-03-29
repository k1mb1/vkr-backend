package com.github.k1mb1.vkr_backend.domain.student_groups;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupRepository
    extends
        JpaRepository<StudentGroupEntity, UUID>,
        JpaSpecificationExecutor<StudentGroupEntity> {

    /** Find a top-level group (no parent) by name, eagerly loading its subgroups. */
    @Query("""
        SELECT DISTINCT g FROM StudentGroupEntity g
        LEFT JOIN FETCH g.subgroups
        WHERE g.name = :name AND g.parentGroup IS NULL
        """)
    Optional<StudentGroupEntity> findWithSubgroupsByNameAndParentGroupIsNull(@Param("name") String name);

    /** Find a subgroup by name under a specific parent group. */
    Optional<StudentGroupEntity> findByNameAndParentGroup_Id(String name, UUID parentGroupId);

    /**
     * Paginated list of main groups (no parent) with total student count across
     * the group and all its subgroups. Single query — no N+1.
     */
    @Query(value = """
        SELECT new com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse(
            g.id, g.name,
            (SELECT COUNT(s) FROM StudentEntity s WHERE s.group = g OR s.group.parentGroup = g),
            (SELECT COUNT(sg) FROM StudentGroupEntity sg WHERE sg.parentGroup = g)
        )
        FROM StudentGroupEntity g
        WHERE g.parentGroup IS NULL
        """,
        countQuery = "SELECT COUNT(g) FROM StudentGroupEntity g WHERE g.parentGroup IS NULL"
    )
    Page<com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse>
        findAllMainGroupsWithStudentCount(Pageable pageable);

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
