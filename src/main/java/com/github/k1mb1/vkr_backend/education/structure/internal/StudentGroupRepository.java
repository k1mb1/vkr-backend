package com.github.k1mb1.vkr_backend.education.structure.internal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupRepository extends JpaRepository<StudentGroupEntity, UUID>, JpaSpecificationExecutor<StudentGroupEntity> {
    Optional<StudentGroupEntity> findByIdAndParentGroupIsNull(UUID id);

    @Query("""
        SELECT DISTINCT g FROM StudentGroupEntity g
        LEFT JOIN FETCH g.students gs
        LEFT JOIN FETCH g.subgroups sg
        LEFT JOIN FETCH sg.students sgs
        WHERE g.id = :id AND g.parentGroup IS NULL
    """)
    Optional<StudentGroupEntity> findWithSubgroupsAndStudentsById(@Param("id") UUID id);
}
