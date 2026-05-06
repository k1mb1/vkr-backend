package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface StudentGroupsRepository
        extends
        JpaRepository<StudentGroup, UUID>,
        JpaSpecificationExecutor<StudentGroup> {

    @Query("""
            SELECT DISTINCT g FROM StudentGroup g
            LEFT JOIN FETCH g.students gs
            LEFT JOIN FETCH g.subgroups sg
            LEFT JOIN FETCH sg.students sgs
            WHERE g.id = :id AND g.parentGroup IS NULL
            """)
    Optional<StudentGroup> findWithSubgroupsAndStudentsById(@Param("id") UUID id);
}
