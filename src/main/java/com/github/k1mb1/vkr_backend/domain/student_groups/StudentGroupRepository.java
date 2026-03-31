package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
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
        JpaSpecificationExecutor<StudentGroupEntity>
{
    @Query(
        """
        SELECT DISTINCT g FROM StudentGroupEntity g
        LEFT JOIN FETCH g.subgroups
        WHERE g.name = :name AND g.parentGroup IS NULL
        """
    )
    Optional<StudentGroupEntity> findWithSubgroupsByNameAndParentGroupIsNull(
        @Param("name") String name
    );

    @Query(
        value = """
        SELECT new com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse(
            g.id, g.name,
            (SELECT COUNT(sg) FROM StudentGroupEntity sg WHERE sg.parentGroup = g)
        )
        FROM StudentGroupEntity g
        WHERE g.parentGroup IS NULL
        """,
        countQuery = "SELECT COUNT(g) FROM StudentGroupEntity g WHERE g.parentGroup IS NULL"
    )
    Page<StudentGroupPageResponse> findAllMainGroupsWithStudentCount(
        Pageable pageable
    );

    @Query(
        """
        SELECT DISTINCT g FROM StudentGroupEntity g
        LEFT JOIN FETCH g.students
        LEFT JOIN FETCH g.subgroups sg
        LEFT JOIN FETCH sg.students
        WHERE g.id = :id AND g.parentGroup IS NULL
        """
    )
    Optional<StudentGroupEntity> findWithSubgroupsAndStudentsById(
        @Param("id") UUID id
    );
}
