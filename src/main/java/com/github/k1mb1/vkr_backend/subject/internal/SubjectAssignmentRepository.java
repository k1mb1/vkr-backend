package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
interface SubjectAssignmentRepository
    extends JpaRepository<SubjectAssignment, UUID> {

    @Query(
        """
            SELECT a FROM SubjectAssignment a
            JOIN FETCH a.teacher
            LEFT JOIN FETCH a.subgroup
            WHERE a.offering.id IN :offeringIds
            """
    )
    List<SubjectAssignment> findByOfferingIdInFetch(
        @Param("offeringIds") Collection<UUID> offeringIds
    );
}